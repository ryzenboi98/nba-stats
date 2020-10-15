package com.example.demo.service;

import com.example.demo.fakedb.MatchDB;
import com.example.demo.model.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatchService {
    private final MatchDB matchDB;

    @Autowired
    public MatchService(@Qualifier("fakeDB") MatchDB matchDB) {
        this.matchDB = matchDB;
    }

    public void createMatches() {
        matchDB.insertMatches();
    }

    public List<Match> getAllMatches() {
        return matchDB.selectAllMatches();
    }

    public Optional<Match> getMatchByID(int id) {
        return matchDB.selectMatchById(id);
    }
}
