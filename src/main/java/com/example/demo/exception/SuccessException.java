package com.example.demo.exception;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SuccessException {
    private final int status;
    private final String message;
    private final ZonedDateTime timestamp;


    public SuccessException(int status, String message, ZonedDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
}

