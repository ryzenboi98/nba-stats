package com.example.demo.service;

import com.example.demo.database.MatchDB;
import com.example.demo.model.Comment;
import com.example.demo.model.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {
    private final MatchDB matchDB;

    @Autowired
    public MatchService(@Qualifier("postgres") MatchDB matchDB) {
        this.matchDB = matchDB;
    }

    public void createMatches() {
        matchDB.insertMatches();
    }

    public List<Match> getMatchesByDate(String date) throws ParseException {
        return matchDB.selectMatchesByDate(date);
    }

    public Optional<Match> getMatchByID(int id) {
        return matchDB.selectMatchById(id);
    }

    public int addComments(int id, List<Comment> comments) {
        return matchDB.insertComments(id, comments);
    }

    public int deleteCommentById(int matchID, int commentID) {
        return matchDB.deleteCommentById(matchID, commentID);
    }

    public int updateCommentById(int matchID, int commentID, Comment comment) {
        return matchDB.updateCommentById(matchID, commentID, comment);
    }
}
