package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class MatchExceptionController {
    @ExceptionHandler(value = MatchDateException.class)
    public ResponseEntity<ExceptionError> handleDateException(MatchDateException e) {
        ExceptionError exceptionError = new ExceptionError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Date is not on the correct format (yyyy-MM-dd) or it doesn't exist",
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(exceptionError, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = MatchDateNotFoundException.class)
    public ResponseEntity<ExceptionError> handleMatchDateNotFoundException(MatchDateNotFoundException e) {
        ExceptionError exceptionError = new ExceptionError(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                "There are no matches for the specified date",
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(exceptionError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = MatchIDNotFoundException.class)
    public ResponseEntity<ExceptionError> handleMatchIDNotFoundException(MatchIDNotFoundException e) {
        ExceptionError exceptionError = new ExceptionError(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                "It doesn't exist a match with the specified ID",
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(exceptionError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = EmptyDataSentException.class)
    public ResponseEntity<ExceptionError> handleMatchIDNotFoundException(EmptyDataSentException e) {
        ExceptionError exceptionError = new ExceptionError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "The data sent is empty or blank",
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(exceptionError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IDNotFoundException.class)
    public ResponseEntity<ExceptionError> handleCommentIDNotFoundException(IDNotFoundException e) {
        ExceptionError exceptionError = new ExceptionError(
                HttpStatus.NOT_FOUND.value(),
                "Bad Request",
                "The match ID or comment ID doesn't exist",
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(exceptionError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = SucessCreateException.class)
    public ResponseEntity<SuccessException> hanldeSucessCreateException(SucessCreateException e) {
        SuccessException successException = new SuccessException(
                HttpStatus.CREATED.value(),
                "The data sent has been successfully created",
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(successException, HttpStatus.CREATED);
    }

}
