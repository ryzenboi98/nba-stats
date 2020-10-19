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
        String sql_count = "select COUNT(*) from matches;";

        int rows = jdbcTemplate.queryForObject(sql_count, Integer.class);

        if(rows != 0)
            return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://rapidapi.p.rapidapi.com/games?page=0&per_page=25"))
                .header("x-rapidapi-host", "free-nba.p.rapidapi.com")
                .header("x-rapidapi-key", "01e155481bmsh90337a24070211fp1b6327jsn44661d7fec09")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(response.body());
        System.out.println("Type: " + response.body().getClass());

        JSONParser parser = new JSONParser();

        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(response.body());
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        JSONArray jsonMatchesArray = (JSONArray) jsonObject.get("data");

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

            Match match = new Match(id, date, homeTeam, visitorTeam, homeScore, visitorScore);

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

    @Override
    public List<Match> selectMatchesByDate(String date) throws ParseException {
        Timestamp t = new Timestamp(System.currentTimeMillis());

        return List.of(new Match(1, t, "Lakers", "LA Star", 101,122));
    }

    @Override
    public Optional<Match> selectMatchById(int id) {
        return Optional.empty();
    }

    @Override
    public int insertComments(int matchID, List<Comment> comments) {
        return 0;
    }

    @Override
    public int deleteCommentById(int matchID, int commentID) {
        return 0;
    }

    @Override
    public int updateCommentById(int matchID, int commentID, Comment comment) {
        return 0;
    }
}
