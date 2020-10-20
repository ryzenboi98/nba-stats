package com.example.demo.database;

import com.example.demo.model.Comment;
import com.example.demo.model.Match;

import com.example.demo.model.Stat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.*;

@Repository("postgres")
public class MatchDataAccessService implements MatchDB{

    private final JdbcTemplate jdbcTemplate;
    private final String baseURL = "https://rapidapi.p.rapidapi.com/";

    public MatchDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertMatches() {

    }

    @Override
    public List<Match> selectMatchesByDate(String date) throws ParseException {
        String urlPath = "games?";
        String params = "dates[]=" + date;

        // Request for RapidAPI
        String response = requestRapidAPI(urlPath, params);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(response);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        JSONArray jsonMatchesArray = (JSONArray) jsonObject.get("data");

        Iterator<JSONObject> it = jsonMatchesArray.iterator();

        List<Match> matchList = new ArrayList<Match>();

        while (it.hasNext()) {
            JSONObject jsonObj = it.next();

            Match match = getMatchData(jsonObj);
            matchList.add(match);
        }

        return matchList;
    }

    @Override
    public Optional<Match> selectMatchById(int id) {
        String urlPath = "games/";
        String params = String.valueOf(id);

        // Request for RapidAPI
        String response = requestRapidAPI(urlPath, params);

        JSONParser parser = new JSONParser();
        JSONObject jsonObj = null;

        try {
            jsonObj = (JSONObject) parser.parse(response);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        Match match = getMatchData(jsonObj);

        urlPath = "stats/?per_page=100&";
        params = "game_ids[]=" + id;

        response = requestRapidAPI(urlPath, params);

        try {
            jsonObj = (JSONObject) parser.parse(response);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        JSONArray jsonPlayerStats = (JSONArray) jsonObj.get("data");

        Iterator<JSONObject> it = jsonPlayerStats.iterator();

        List<Stat> statsList = new ArrayList<>();

        int sum = 0;
        while(it.hasNext()) {
            JSONObject jsonObject = it.next();

            if(jsonObject.get("pts") != null)
                if(Math.toIntExact((Long) jsonObject.get("pts")) > 0) {
                    Stat stat = new Stat((String) ((JSONObject) jsonObject.get("player")).get("first_name") + " " +
                            ((JSONObject) jsonObject.get("player")).get("last_name"), Math.toIntExact((Long) jsonObject.get("pts")));

                    statsList.add(stat);
                }
        }

        statsList.sort(Comparator.comparing(Stat::getPoints).reversed());

        for(Stat s : statsList) {
            match.addStat(s);
        }

        return Optional.ofNullable(match);
    }

    @Override
    public int insertComments(int matchID, List<Comment> comments) {
        String sql_count = "select count(*) from matches where match_id = ?;";

        int matchRows = jdbcTemplate.queryForObject(sql_count, new Object[] {matchID}, Integer.class);

        sql_count = "select count(*) from comments;";

        int commentsRows = jdbcTemplate.queryForObject(sql_count, Integer.class);

        if(matchRows != 0) {
            int id = 1;

            if(commentsRows != 0) {
                String sql_id = "select max(comment_id) from comments where match_id = ?;";
                id = jdbcTemplate.queryForObject(sql_id, new Object[]{matchID}, Integer.class) + 1;
            }

            String sql = "" +
                    "INSERT INTO Comments (" +
                    " comment_id, " +
                    " match_id, " +
                    " comment, " +
                    " c_date) " +
                    "VALUES (?, ?, ?, ?);";

            if(!comments.isEmpty()) {
                System.out.println("YOOOOOOO ITS ME");
                Timestamp time = new Timestamp(System.currentTimeMillis());

                for(Comment com : comments) {
                    jdbcTemplate.update(
                            sql,
                            id,
                            matchID,
                            com.getMessage(),
                            time
                    );
                    id++;
                }
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public int deleteCommentById(int matchID, int commentID) {
        String sql_count = "select count(*) from comments where match_id = ? and comment_id = ?;";

        int rows = jdbcTemplate.queryForObject(sql_count, new Object[] {matchID, commentID}, Integer.class);

        if(rows > 0) {
            String sql = "delete from comments where match_id = ? and comment_id = ?;";
            jdbcTemplate.update(sql, matchID, commentID);

            return 1;
        } else {
           return 0;
        }
    }

    @Override
    public int updateCommentById(int matchID, int commentID, Comment comment) {
        String sql_count = "select count(*) from comments where match_id = ? and comment_id = ?;";

        System.out.println("CommentID -> " + commentID);

        int rows = jdbcTemplate.queryForObject(sql_count, new Object[] {matchID, commentID}, Integer.class);

        if(rows > 0) {
            String sql = "update comments set comment = ?, c_date = ? where match_id = ? and comment_id = ?;";

            System.out.println(getCurrentTime());
            jdbcTemplate.update(sql, comment.getMessage(), getCurrentTime(), matchID, commentID);

            return 1;
        } else {
            return 0;
        }
    }

    private boolean databaseIsEmpty() {
        String sql_count = "select COUNT(*) from matches;";

        int rows = jdbcTemplate.queryForObject(sql_count, Integer.class);

        if(rows > 0)
            return false;
        else
            return true;
    }



    private String requestRapidAPI(String urlPath, String params) {
        // Request for RapidAPI
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

        return response.body();
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
        Match match = new Match(id, homeTeam, visitorTeam, homeScore, visitorScore, d);

        return  match;
    }

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

    private void insertMatchesData(JSONArray jsonMatchesArray) {
        Iterator<JSONObject> it =  jsonMatchesArray.iterator();

        while (it.hasNext()) {
            JSONObject jsonObj = it.next();

            int id = Math.toIntExact((Long) jsonObj.get("id"));

            //Timestamp date = Timestamp.valueOf((String) jsonObj.get("date"));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS UTC");

            Date d = new Date();
            Timestamp date;
            //LocalDateTime date = LocalDateTime.parse((String) jsonObj.get("date"));

            try {
                d = format.parse((String) jsonObj.get("date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date = new Timestamp(d.getTime());

            String homeTeam = (String) ((JSONObject) jsonObj.get("home_team")).get("full_name");
            String visitorTeam = (String) ((JSONObject) jsonObj.get("visitor_team")).get("full_name");
            int homeScore = Math.toIntExact((Long) jsonObj.get("home_team_score"));
            int visitorScore = Math.toIntExact((Long) jsonObj.get("visitor_team_score"));

            Match match = new Match(id, homeTeam, visitorTeam, homeScore, visitorScore, date);

            String sql = "" +
                    "INSERT INTO Matches (" +
                    " match_id, " +
                    " home_team, " +
                    " visitor_team, " +
                    " home_score, " +
                    " visitor_score," +
                    " m_date)" +
                    "VALUES (?, ?, ?, ?, ?, ?);";

            jdbcTemplate.update(
                    sql,
                    match.getId(),
                    match.getHomeTeam(),
                    match.getVisitorTeam(),
                    match.getHomeScore(),
                    match.getVisitorScore(),
                    match.getDate()
            );
        }
    }

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
