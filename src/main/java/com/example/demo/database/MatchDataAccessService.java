package com.example.demo.database;

import com.example.demo.model.Comment;
import com.example.demo.model.Match;

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

    public MatchDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertMatches() {
        // Check if there's data on the database
        if(!databaseIsEmpty())
            return;

        // Request for all the first 25 NBA matches
        String response = getMatchesRapidAPI();

        // Parse response to Matches JSON data
        JSONArray jsonMatchesArray = parseMatchesData(response);

        // Insertion of the all the Matches in the postgresql database
        insertMatchesData(jsonMatchesArray);
    }

    @Override
    public List<Match> selectMatchesByDate(String date) throws ParseException {

        List<Match> matches = null;

        if(date != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date dt = fmt.parse((String) date);

            String sql = "select * " +
                    "from matches where m_date = ?;";


            matches = jdbcTemplate.query(sql, new Object[]{dt}, (resultSet, i) -> {
                int id = Integer.parseInt(resultSet.getString("match_id"));
                String homeTeam = resultSet.getString("home_team");
                String visitorTeam = resultSet.getString("visitor_team");
                int homeScore = Integer.parseInt(resultSet.getString("home_score"));
                int visitorScore = Integer.parseInt(resultSet.getString("visitor_score"));
                // Make parse String to Date function
                Timestamp dat = parseStringToTimestamp(resultSet.getString("m_date"));

                return new Match(id, homeTeam, visitorTeam, homeScore, visitorScore, dat);
            });
        }
        else {
            String sql = "select * from matches;";

            matches = jdbcTemplate.query(sql, (resultSet, i) -> {
                int id = Integer.parseInt(resultSet.getString("match_id"));
                String homeTeam = resultSet.getString("home_team");
                String visitorTeam = resultSet.getString("visitor_team");
                int homeScore = Integer.parseInt(resultSet.getString("home_score"));
                int visitorScore = Integer.parseInt(resultSet.getString("visitor_score"));
                // Make parse String to Date function
                Timestamp dat = parseStringToTimestamp(resultSet.getString("m_date"));

                return new Match(id, homeTeam, visitorTeam, homeScore, visitorScore, dat);
            });
        }

        for(Match m : matches) {
            String sql_ = "select * from comments where match_id = ? order by c_date desc;";

            List<Comment> comments = jdbcTemplate.query(sql_, new Object[]{m.getId()}, (resultSet, i) -> {
                int commentID = Integer.parseInt(resultSet.getString("comment_id"));
                String message = resultSet.getString("comment");
                Timestamp time = parseStringToTimestamp(resultSet.getString("c_date"));

                return new Comment(commentID, message, time);
            });

            for (Comment com : comments)
                m.addComment(com);
        }
        return matches;
    }

    @Override
    public Optional<Match> selectMatchById(int id) {
        String sql = "select * from matches where match_id = ?;";

        Match match = jdbcTemplate.queryForObject(sql, new Object[] {id},
                (resultSet, i) -> {
                    int matchID = Integer.parseInt(resultSet.getString("match_id"));
                    String homeTeam = resultSet.getString("home_team");
                    String visitorTeam = resultSet.getString("visitor_team");
                    int homeScore = Integer.parseInt(resultSet.getString("home_score"));
                    int visitorScore = Integer.parseInt(resultSet.getString("visitor_score"));
                    Timestamp date = parseStringToTimestamp(resultSet.getString("m_date"));

                    return new Match(matchID, homeTeam, visitorTeam, homeScore, visitorScore, date);
                });

        sql = "select * from comments where match_id = ? order by c_date desc;";

        List<Comment> comments = jdbcTemplate.query(sql, new Object[] {id}, (resultSet, i) -> {
            int commentID = Integer.parseInt(resultSet.getString("comment_id"));
            String message = resultSet.getString("comment");
            Timestamp time = parseStringToTimestamp(resultSet.getString("c_date"));

            return new Comment(commentID, message, time);
        });

        for(Comment com : comments)
            match.addComment(com);

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


    private String getMatchesRapidAPI() {
        // Request for all the first 25 NBA matches
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://rapidapi.p.rapidapi.com/games?page=0&per_page=25"))
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
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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

    private Timestamp parseStringToTimestamp(String date) {
        Date d = new Date();
        Timestamp dat;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            d = format.parse((String) date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dat = new Timestamp(d.getTime());

        return dat;
    }
}
