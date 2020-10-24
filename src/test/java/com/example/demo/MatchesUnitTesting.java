package com.example.demo;

import com.example.demo.api.MatchController;

import com.example.demo.database.MatchDataAccessService;
import com.example.demo.exception.MatchDateException;
import com.example.demo.exception.MatchDateNotFoundException;
import com.example.demo.exception.MatchIDNotFoundException;
import com.example.demo.model.Match;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class MatchesUnitTesting {

    @Autowired
    private MatchDataAccessService matchDataAccessService;

    @Test
    public void getMatchByIdNotFound() {
        assertThrows(MatchIDNotFoundException.class, () -> {
           Optional<Match> match = matchDataAccessService.selectMatchById(32948092);
        });
    }

    @Test
    public void getMatchByIdSuccess() {
        Optional<Match> match = matchDataAccessService.selectMatchById(1) ;
        assertTrue(match.isPresent());
    }

    @Test
    public void getMatchesByNonExistDate() {

        assertThrows(MatchDateException.class, () -> {
            List<Match> matches = matchDataAccessService.selectMatchesByDate("11454-23-12");
        });
    }

    @Test
    public void getMatchesByWrongFormatDate() {

        assertThrows(MatchDateException.class, () -> {
            List<Match> matches = matchDataAccessService.selectMatchesByDate("12-10-2019") ;
        });
    }

    @Test
    public void getMatchesByDateNotFound() {
        assertThrows(MatchDateNotFoundException.class, () -> {
            List<Match> matches = matchDataAccessService.selectMatchesByDate("2021-01-07");
        });
    }

    @Test
    public void getMatchByDateSuccess() throws ParseException {

            List<Match> matches = matchDataAccessService.selectMatchesByDate("2020-08-25");

            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date date = (Date) fmt.parse("2020-08-25");

            for(Match match : matches) {
                Date matchDate = (Date) fmt.parse(String.valueOf(match.getDate()));
                //System.out.println("Match Date -> " + matchDate);
                //System.out.println("Match Date -> " + date);
                assertTrue(matchDate.equals(date));
            }
    }


}
