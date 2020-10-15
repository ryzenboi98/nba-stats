package com.example.demo.api;

import com.example.demo.model.Match;
import com.example.demo.service.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("api/nba/match")
@RestController
public class MatchController {
    private final MatchService matchService;


    public MatchController(MatchService matchService) {
        this.matchService = matchService;
        matchService.createMatches();
    }

    @GetMapping
    public List<Match> getAllMatches() {
        return matchService.getAllMatches();
    }

    @GetMapping(path = "{id}")
    public Match getMatchByID(@PathVariable("id") int id) {
        return matchService.getMatchByID(id)
                .orElse(null);
    }
}
