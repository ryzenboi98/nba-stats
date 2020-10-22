package com.example.demo.exception;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ExceptionError {
    private final int status;
    private final String error;
    private final String message;
    private final ZonedDateTime timestamp;


    public ExceptionError(int status, String error, String message, ZonedDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
}

