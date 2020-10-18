package com.example.demo.fakedb;

import com.example.demo.model.Comment;
import com.example.demo.model.Match;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MatchDB {
    void insertMatches();
    List<Match> selectMatchesByDate(String date) throws ParseException;
    Optional <Match> selectMatchById(int id);

    int insertComments(int matchID, List<Comment> comments);
    int deleteCommentById(int matchID, int commentID);
    int updateCommentById(int matchID, int commentID, Comment comment);
}
