package com.example.demo.model;

public class Stat {
    private final String playerName;
    private final int points;

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
}
