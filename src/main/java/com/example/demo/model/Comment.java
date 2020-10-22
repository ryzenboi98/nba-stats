package com.example.demo.model;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;


public class Comment {
    private final int id;
    @NotBlank
    private String message;
    private final Timestamp date;

    public Comment(int id, String message, Timestamp date) {
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

    public void setMessage(String m) {
        message = m;
    }


}
