package com.example.demo.database;

import com.example.demo.exception.EmptyDataSentException;
import com.example.demo.exception.MatchDateException;
import com.example.demo.exception.MatchDateNotFoundException;
import com.example.demo.exception.MatchIDNotFoundException;
import com.example.demo.model.Comment;
import com.example.demo.model.Match;

import com.example.demo.model.Stat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.swing.plaf.synth.SynthDesktopIconUI;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.*;

@Repository("postgres")
public class MatchDataAccessService implements MatchDB{

    private final JdbcTemplate jdbcTemplate;

    public MatchDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertMatches() {

    }

    //@Cacheable(value = "Matches")
    @Override
    public List<Match> selectMatchesByDate(String date) throws ParseException {
        String urlPath = "games?";
        String params = "dates[]=" + date;

        List<Integer> matchesIDs = new ArrayList<>();
        List<Match> matchList = new ArrayList<Match>();
        List<Stat> statsList = new ArrayList<>();

        if(!dateIsValid(date)) {
            System.out.println("Bad request for date");
            throw new MatchDateException();
        }

        // Request for RapidAPI
        String response = requestRapidAPI(urlPath, params);

        // Add all matches requested from the RapidAPI and all comments requested from DB
        addMatchComsRapidAPI(response, matchList, matchesIDs);

        if(matchList.isEmpty()) {
            throw new MatchDateNotFoundException();
        }

        urlPath = "stats/?per_page=100";
        params = "";

        for(Integer matchID : matchesIDs)
            params = params + "&game_ids[]=" + matchID;

        // Get all Stats for the returned Matches
        response = requestRapidAPI(urlPath, params);

        // Add all Stats from the first page result
        addStatsFirstPage(response, statsList);

        // Add all Stats for the rest pages results
        addStatsNextPages(response, statsList, params);

        // Add all Stats for each correspondent Match
        addAllStatsToMatches(matchList, statsList);

        return matchList;
    }

    //@Cacheable(value = "Match", key = "#id")

    @Override
    public Optional<Match> selectMatchById(int id) {
        String urlPath = "games/";

        String params = String.valueOf(id);

        List<Stat> statsList = new ArrayList<>();
        List<Comment> comments;

        // Request for RapidAPI
        String response = requestRapidAPI(urlPath, params);

        if(response == null || response.equals("")) {
            throw new MatchIDNotFoundException();
        }

        JSONObject jsonObj = parseStringToJSON(response);

        System.out.println("Getting match data...");
        //Get the data for the Match specified by ID
        Match match = getMatchData(jsonObj);

        urlPath = "stats/?per_page=100&";
        params = "game_ids[]=" + id;

        response = requestRapidAPI(urlPath, params);

        addStatsFirstPage(response, statsList);

        // Sort stats list by player points
        statsList.sort(Comparator.comparing(Stat::getPoints).reversed());
        match.addStats(statsList);

        System.out.println("Getting comments from database...");
        comments = getCommentsByMatchID(id);
        match.addComments(comments);

        return Optional.of(match);
    }

    @Override
    public int insertComments(int matchID, List<Comment> comments) {
        // Comments validation
        validateComments(comments);

        Optional<Match> m = selectMatchById(matchID);

        if(m.isPresent()) {
            // Get comment ID for insertion
            int id = getInsertionID(matchID);

            // Insert comments in the database
            insertCommentsDB(comments, id, matchID);

            return 1;
        } else {
            // There is no Match with such ID
            if(!m.isPresent())
                throw new MatchIDNotFoundException();
            else
                throw new EmptyDataSentException();
        }
    }

    public int updateCommentById(int matchID, int commentID, Comment comment) {
        if(comment == null)
            throw new EmptyDataSentException();

        if(comment.getMessage().trim().isEmpty())
            throw new EmptyDataSentException();
        else
            comment.setMessage(comment.getMessage().trim());

        String sql_count = "select count(*) from comments where match_id = ? and comment_id = ?;";

        int rows = jdbcTemplate.queryForObject(sql_count, new Object[] {matchID, commentID}, Integer.class);

        if(rows > 0) {
            String sql = "update comments set comment = ?, c_date = ? where match_id = ? and comment_id = ?;";

            System.out.println("Updating comment...");

            jdbcTemplate.update(sql, comment.getMessage(), getCurrentTime(), matchID, commentID);
            return 1;
        } else {
            throw new MatchIDNotFoundException();
        }
    }

    //@Cacheable(value = "Match", key = "#id")
    public int deleteCommentById(int matchID, int commentID) {
        String sql_count = "select count(*) from comments where match_id = ? and comment_id = ?;";

        int rows = jdbcTemplate.queryForObject(sql_count, new Object[] {matchID, commentID}, Integer.class);

        if(rows > 0) {
            String sql = "delete from comments where match_id = ? and comment_id = ?;";
            jdbcTemplate.update(sql, matchID, commentID);

            return 1;
        } else {
            throw new MatchIDNotFoundException();
        }
    }

    private String requestRapidAPI(String urlPath, String params) {
        // Request for RapidAPI
        String baseURL = "https://rapidapi.p.rapidapi.com/";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + urlPath + params))
                .header("x-rapidapi-host", "free-nba.p.rapidapi.com")
                .header("x-rapidapi-key", "01e155481bmsh90337a24070211fp1b6327jsn44661d7fec09")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        assert response != null;
        return response.body();
    }

    private List<Comment> getCommentsByMatchID(int matchID) {
        String sql = "select * from comments where match_id = ? order by c_date desc;";

        return jdbcTemplate.query(sql, new Object[] {matchID},
                ((resultSet, i) -> {
                    int comment_id = Integer.parseInt(resultSet.getString("comment_id"));
                    String message = resultSet.getString("comment");
                    Timestamp d = parseStringToTimestamp(resultSet.getString("c_date"), null);

                    return new Comment(comment_id, message, d);
                })
        );
    }

    private boolean playerScoredPoints(JSONObject jsonObject) {
        if (jsonObject.get("pts") != null)
            if(Math.toIntExact((Long) jsonObject.get("pts")) > 0)
                return true;
            else
                return false;
        else
            return false;
    }

    private Match getMatchData(JSONObject jsonObject) {
        int id = Math.toIntExact((Long) jsonObject.get("id"));
        String homeTeam = (String) ((JSONObject) jsonObject.get("home_team")).get("full_name");
        String visitorTeam = (String) ((JSONObject) jsonObject.get("visitor_team")).get("full_name");
        int homeScore = Math.toIntExact((Long) jsonObject.get("home_team_score"));
        int visitorScore = Math.toIntExact((Long) jsonObject.get("visitor_team_score"));

        SimpleDateFormat format;

        if(jsonObject.get("date").toString().contains("Z"))
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        else
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        Timestamp d = parseStringToTimestamp((String) jsonObject.get("date"), format);

        return new Match(id, homeTeam, visitorTeam, homeScore, visitorScore, d);
    }

    private Stat getStatData(JSONObject jsonObject) {
        Stat stat = new Stat((String) ((JSONObject) jsonObject.get("player")).get("first_name") + " " +
                        ((JSONObject) jsonObject.get("player")).get("last_name"), Math.toIntExact((Long) jsonObject.get("pts")));

        int matchID = (Math.toIntExact((Long) ((JSONObject) jsonObject.get("game")).get("id")));
        stat.setMatchID(matchID);

        return stat;

    }

    private void addAllStatsToMatches(List<Match> matchList, List<Stat> statsList) {
        for (Match m : matchList) {
            List<Stat> stats = new ArrayList<>();
            for (Stat s : statsList) {
                if (s.getMatchID() == m.getId()) {
                    stats.add(s);
                }
            }
            stats.sort(Comparator.comparing(Stat::getPoints).reversed());
            m.addStats(stats);
        }
    }

    private void addMatchComsRapidAPI(String response, List<Match> matchList, List<Integer> matchesIDs) {

        JSONObject jsonObject = parseStringToJSON(response);
        JSONArray jsonMatchesArray = (JSONArray) jsonObject.get("data");

        Iterator<JSONObject> it = jsonMatchesArray.iterator();

        while (it.hasNext()) {
            JSONObject jsonObj = it.next();

            matchesIDs.add(Math.toIntExact((Long) jsonObj.get("id")));
            Match match = getMatchData(jsonObj);

            // Get all comments and add to the specific Match
            List<Comment> comments = getCommentsByMatchID(match.getId());
            match.addComments(comments);

            matchList.add(match);
        }
        System.out.println("Getting comments from database...");
        System.out.println("Getting matches from database...");

    }

    private JSONObject parseStringToJSON(String response) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(response);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /*
    private JSONArray parseMatchesData(String response) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(response);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        JSONArray jsonMatchesArray = (JSONArray) jsonObject.get("data");

        return jsonMatchesArray;
    }
    */

    private void addStatsFirstPage(String response, List<Stat> statsList) {
        JSONObject jsonObject = parseStringToJSON(response);
        JSONArray statsArray = (JSONArray) jsonObject.get("data");

        Iterator<JSONObject> it = statsArray.iterator();

        while(it.hasNext()) {
            JSONObject jsonObj = it.next();

            //System.out.println("Object value: ->" + jsonObject.get("pts") + "   pts -> " + Math.toIntExact((Long) jsonObject.get("pts")));

            // Check if players scored points in the match
            if(playerScoredPoints(jsonObj)) {
                Stat stat = getStatData(jsonObj);
                statsList.add(stat);
            }
        }
    }

    private void addStatsNextPages(String response, List<Stat> statsList, String params) {
        JSONObject jsonObject = parseStringToJSON(response);
        JSONArray statsArray = (JSONArray) jsonObject.get("data");

        int totalPages = Math.toIntExact((Long) (((JSONObject) jsonObject.get("meta")).get("total_pages")));

        for(int i = 1; i < totalPages; i++) {
            String urlPath = "stats/?per_page=100" + "&page=" + (i+1);

            response = requestRapidAPI(urlPath, params);

            jsonObject = parseStringToJSON(response);
            statsArray = (JSONArray) jsonObject.get("data");

            Iterator<JSONObject> it = statsArray.iterator();

            while(it.hasNext()) {
                JSONObject jsonObj = it.next();

                if(playerScoredPoints(jsonObj)) {
                    Stat stat = getStatData(jsonObj);
                    statsList.add(stat);
                }
            }
        }
    }

    private void insertCommentsDB(List<Comment> comments, int id, int matchID) {
        String sql = "" +
                "INSERT INTO Comments (" +
                " comment_id, " +
                " match_id, " +
                " comment, " +
                " c_date) " +
                "VALUES (?, ?, ?, ?);";

        for(Comment com : comments) {
            jdbcTemplate.update(
                    sql,
                    id,
                    matchID,
                    com.getMessage(),
                    getCurrentTime()
            );
            id++;
        }
    }

    private int getInsertionID(int matchID) {
        int id = 1;

        // Get number of Comments rows for a the specified Match
        String sql_count = "select count(*) from comments where match_id = ?;";
        int commentsRows = jdbcTemplate.queryForObject(sql_count, new Object[] {matchID} , Integer.class);

        if(commentsRows != 0) {
            String sql_id = "select max(comment_id) from comments where match_id = ?;";
            id = jdbcTemplate.queryForObject(sql_id, new Object[]{matchID}, Integer.class) + 1;
        }

        return id;
    }

    private boolean dateIsValid(String date) {
        String[] arrDateParams = date.split("-", 3);

        if(arrDateParams.length != 3 || arrDateParams[0].length() != 4
                || arrDateParams[1].length() != 2 || arrDateParams[2].length() != 2) {
            return false;
        }

        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        fmt.setLenient(false);

        try {
            fmt.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private void validateComments(List<Comment> comments) {
        if(comments.isEmpty())
            throw new EmptyDataSentException();

        for(Comment c : comments)
            if(c.getMessage().trim().isEmpty())
                throw new EmptyDataSentException();
            else {
                String s = c.getMessage().trim();
                c.setMessage(s);
            }
    }

    /*
    private boolean idIsValid(int id) {

    }

     */

    private Timestamp getCurrentTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    private Timestamp parseStringToTimestamp(String date, SimpleDateFormat format) {
        Date d = new Date();
        Timestamp dat;


        if(format == null) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }

        try {
            d = format.parse((String) date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dat = new Timestamp(d.getTime());

        //dat = Timestamp.valueOf(date);

        return dat;
    }
}
