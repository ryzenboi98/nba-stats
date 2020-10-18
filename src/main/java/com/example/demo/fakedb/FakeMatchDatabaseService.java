package com.example.demo.fakedb;

import com.example.demo.model.Comment;
import com.example.demo.model.Match;
import org.springframework.stereotype.Repository;

import javax.swing.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("fakeDB")
public class FakeMatchDatabaseService implements MatchDB {

    private static List<Match> matchDB = new ArrayList<>();

    @Override
    public void insertMatches() {
        Match m1 = new Match(1, new Timestamp(System.currentTimeMillis()),
                "Lakers", "Angels", 112, 108);
        Match m2 = new Match(2, new Timestamp(System.currentTimeMillis()),
                "Sanderns", "Diab", 79, 90);
        Match m3 = new Match(3, new Timestamp(System.currentTimeMillis()),
                "Qertz", "Giants", 130, 112);
        Match m4 = new Match(4, new Timestamp(System.currentTimeMillis()),
                "LA", "Thiefs", 100, 106);
        Match m5 = new Match(5, new Timestamp(System.currentTimeMillis()),
                "Thiefs", "Lakers", 112, 98);
        Match m6 = new Match(6, new Timestamp(System.currentTimeMillis()),
                "Qertz", "Angels", 107, 100);
        Match m7 = new Match(7, new Timestamp(System.currentTimeMillis()),
                "Diab", "Sanderns", 99, 97);

        matchDB.add(m1);
        matchDB.add(m2);
        matchDB.add(m3);
        matchDB.add(m4);
        matchDB.add(m5);
        matchDB.add(m6);
        matchDB.add(m7);
    }

    @Override
    public List<Match> selectMatchesByDate(String date) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");

        System.out.println("OK");

        if(date == null) {
            return matchDB;
        } else {
            Date d = fmt.parse(date);

            return matchDB.stream()
                    .filter(match -> fmt.format(match.getDate()).equals(fmt.format(d)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Optional<Match> selectMatchById(int id) {

        System.out.println("OKEY");
        return matchDB.stream()
                .filter(match -> match.getId() == id)
                .findFirst();
    }

    @Override
    public int insertComments(int matchID, List<Comment> comments) {
        Match match = null;

        System.out.println(matchID);

        comments.forEach(System.out::println);
        //System.out.println(comment.getMessage());

        for(Match m : matchDB) {
            if(m.getId() == matchID) {
                match = m;
            }
        }

        if(match != null) {
            List<Comment> coms = match.getAllComments();
            int id = 1;
            Timestamp t = new Timestamp(System.currentTimeMillis());

            if(coms.size() != 0) {
                //System.out.println("YOLLO");
                id = coms.get(coms.size() - 1).getId() + 1;

                for(Comment com : comments) {
                    Comment c = new Comment(id, com.getMessage(), t);
                    match.addComment(c);
                }

            } else {
                //System.out.println("YOLLO2");
                for(Comment com : comments) {
                    Comment c = new Comment(id, com.getMessage(), t);
                    match.addComment(c);
                }
            }
            return 1;
        } else
            return 0;
    }
}
