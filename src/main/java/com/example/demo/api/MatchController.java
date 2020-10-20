package com.example.demo.api;

import com.example.demo.model.Comment;
import com.example.demo.model.Match;
import com.example.demo.service.MatchService;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
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
    public List<Match> getMatchesByDate(@Valid @NotNull @RequestParam(required = false) String date) throws ParseException {
        return matchService.getMatchesByDate(date);
    }

    @GetMapping(path="{id}")
    public Match getMatchByID(@Valid @NotNull @PathVariable("id") int id) {
        return matchService.getMatchByID(id)
                .orElse(null);
    }

    @PostMapping(value="/{id}/comment")
    public int addComments(@PathVariable("id") int id,  @Valid @NotNull @RequestBody List<Comment> comments) {
        return matchService.addComments(id, comments);
    }

    @DeleteMapping(value = "/{id_m}/comment/{id_c}")
    public int deleteComment(@PathVariable("id_m") int matchID, @PathVariable("id_c") int commentID) {
        return matchService.deleteCommentById(matchID, commentID);
    }

    @PutMapping(value = "/{id_m}/comment/{id_c}")
    public int updateComment(@PathVariable("id_m") int matchID, @PathVariable("id_c") int commentID,
                             @Valid @NotNull @RequestBody Comment comment) {
        return matchService.updateCommentById(matchID, commentID, comment);
    }
}
