package com.example.demo.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Match {
    private int id;
    private final Timestamp date;
    private final String homeTeam;
    private final String visitorTeam;
    private final int homeScore;
    private final int visitorScore;
    private final List<Comment> comments = new ArrayList<Comment>();
    private final List<Stat> stats = new ArrayList<Stat>();

    public Match(int id,
                 Timestamp date,
                 String homeTeam,
                 String visitorTeam,
                 int homeScore,
                 int visitorScore) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.visitorTeam = visitorTeam;
        this.homeScore = homeScore;
        this.visitorScore = visitorScore;
    }

    public int getId() {
        return id;
    }

    public Timestamp getDate() {
        return date;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getVisitorScore() {
        return visitorScore;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getVisitorTeam() {
        return visitorTeam;
    }

    public void addComment(Comment comment) {
        comments.add(0, comment);
    }

    public List<Comment> getAllComments() {
        return comments;
    }

    public Comment getCommentById(int id) {
        for (Comment comment : comments) {
            if (comment.getId() == id) {
                return comment;
            }

        }
        return null;
    }

    public void deleteComment(Comment com) {
        comments.remove(com);
    }

    public void updateComment(int id, Comment com) {
        Comment c = getCommentById(id);
        int id_c = c.getId();
        int index = comments.indexOf(c);

        System.out.println("index = " + index);

        comments.set(index, new Comment(id_c, com.getMessage(), new Timestamp(System.currentTimeMillis())));
    }

    public void addStat(Stat stat) {
        stats.add(stat);
    }

    public List<Stat> getAllStats() {
        return stats;
    }

}
