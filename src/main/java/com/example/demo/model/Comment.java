package com.example.demo.model;

import java.sql.Timestamp;

public class Comment {
    private final int id;
    private final String message;
    private final Timestamp date;

    public Comment(int matchID, int id, String message, Timestamp date) {
        this.id = id;

        this.message = message;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getDate() {
        return date;
    }
}
