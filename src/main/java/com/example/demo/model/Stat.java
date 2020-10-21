package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Stat {
    private final String playerName;
    private final int points;
    @JsonIgnore
    private int matchID;

    public Stat(String playerName, int points) {
        this.playerName = playerName;
        this.points = points;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPoints() {
        return points;
    }


    public void setMatchID(int matchID) {
        this.matchID = matchID;
    }

    public int getMatchID() {
        return matchID;
    }
}
