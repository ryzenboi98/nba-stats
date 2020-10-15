package com.example.demo.model;

import java.sql.Timestamp;

public class Match {
    private int id;
    private final Timestamp date;
    private final String homeTeam;
    private final String visitorTeam;
    private final int homeScore;
    private final int visitorScore;

    public Match(int id, Timestamp date, String homeTeam, String visitorTeam, int homeScore, int visitorScore) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.visitorTeam = visitorTeam;
        this.homeScore = homeScore;
        this.visitorScore = visitorScore;
    }

    public int getId() {
        return id;
    }

    public Timestamp getDate() {
        return date;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getVisitorScore() {
        return visitorScore;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getVisitorTeam() {
        return visitorTeam;
    }
}
