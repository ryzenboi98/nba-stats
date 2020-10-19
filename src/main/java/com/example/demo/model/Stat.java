package com.example.demo.model;

public class Stat {
    private final int i;
    private final String playerName;
    private final String points;

    public Stat(int i, String playerName, String points) {
        this.i = i;
        this.playerName = playerName;
        this.points = points;
    }

    public int getI() {
        return i;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPoints() {
        return points;
    }
}
