package com.example.demo.api;

import com.example.demo.model.Match;
import com.example.demo.service.MatchService;

import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
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
    public List<Match> getMatchesByDate(@RequestParam(required = false) String date) throws ParseException {
        return matchService.getMatchesByDate(date);
    }

    @GetMapping(path="{id}")
    public Match getMatchByID(@PathVariable("id") int id) {
        return matchService.getMatchByID(id)
                .orElse(null);
    }
}
