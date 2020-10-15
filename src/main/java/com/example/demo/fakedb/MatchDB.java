package com.example.demo.fakedb;

import com.example.demo.model.Match;

import java.util.List;
import java.util.Optional;

public interface MatchDB {
    void insertMatches();
    List<Match> selectAllMatches();
    Optional<Match> selectMatchById(int id);
}
