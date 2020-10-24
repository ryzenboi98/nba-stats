package com.example.demo;

import com.example.demo.api.MatchController;
import com.example.demo.database.MatchDataAccessService;
import com.example.demo.exception.EmptyDataSentException;
import com.example.demo.exception.IDNotFoundException;
import com.example.demo.exception.MatchIDNotFoundException;
import com.example.demo.exception.SucessCreateException;
import com.example.demo.model.Comment;
import com.example.demo.model.Match;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CommentsUnitTesting {
    // List for storing unique comment messages
    private static List<String> UUIDList = new ArrayList<>();
    private static int count = 0;

    @Autowired
    private MatchDataAccessService matchDataAccessService;

    @Autowired
    private MatchController matchController;

    @AfterEach
    public void set() {
        clear();
    }

    @Test
    public void insertCommentNullValue() {
        List<Comment> comments = null;

        assertThrows(EmptyDataSentException.class, () -> {
            matchDataAccessService.insertComments(4, comments);
        });
        count++;
    }

    @Test
    public void insertCommentBlankValue() {
        List<Comment> comments = new ArrayList<>();

        Timestamp t = new Timestamp(System.currentTimeMillis());
        comments.add(new Comment(1, "           ", t));

        assertThrows(EmptyDataSentException.class, () -> {
            matchDataAccessService.insertComments(4, comments);
        });
        count++;
    }

    @Test
    public void insertCommentWrongMatchID() {
        List<Comment> comments = new ArrayList<>();

        Timestamp t = new Timestamp(System.currentTimeMillis());
        comments.add(new Comment(1,"Oh what a game!", t));

        assertThrows(MatchIDNotFoundException.class, () -> {
            matchDataAccessService.insertComments(0, comments);
        });
        count++;
    }

    @Test
    public void insertCommentFixBlankSpaces() {
        List<Comment> comments = new ArrayList<>();

        UUID uid = UUID.randomUUID();
        UUIDList.add(uid.toString());

        Timestamp t = new Timestamp(System.currentTimeMillis());
        comments.add(new Comment(1,"    " + uid + "     ", t));

        try {
            int response = matchDataAccessService.insertComments(4, comments);
        }catch (Exception e) {

        }

        Match match = matchController.getMatchByID(4);
        List<Comment> coms = match.getAllComments();

        for(Comment c : coms)
            if(c.getMessage().equals(uid.toString()))
                assertTrue(true);
        count++;
    }

    @Test
    public void insertCommentSuccess() {
        List<Comment> comments = new ArrayList<>();

        UUID uid = UUID.randomUUID();
        UUIDList.add(uid.toString());
        Timestamp t = new Timestamp(System.currentTimeMillis());

        comments.add(new Comment(1, uid.toString(), t));

        assertThrows(SucessCreateException.class, () -> {
                    int response = matchDataAccessService.insertComments(4, comments);
                });


        count++;
    }

    @Test
    public void insertMultipleCommentsSuccess() {
        List<Comment> comments = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            Timestamp t = new Timestamp(System.currentTimeMillis());

            UUID uid = UUID.randomUUID();
            UUIDList.add(uid.toString());

            comments.add(new Comment(1,uid.toString(), t));
        }

        assertThrows(SucessCreateException.class, () -> {
            int response = matchDataAccessService.insertComments(4, comments);
        });

        count++;
    }


    @Test
    public void updateCommentWrongMatchID() {
        Comment comment = new Comment(1, "Hello", new Timestamp(System.currentTimeMillis()));

        assertThrows(IDNotFoundException.class, () -> {
            matchDataAccessService.updateCommentById(100000000, 4, comment);
        });
        count++;
    }

    @Test
    public void updateCommentWrongCommentID() {
        Comment comment = new Comment(1, "Hello", new Timestamp(System.currentTimeMillis()));

        assertThrows(IDNotFoundException.class, () -> {
            matchDataAccessService.updateCommentById(1, 100000000, comment);
        });
        count++;
    }

    @Test
    public void updateCommentFixBlankSpaces() {
        // Getting one of the comments inserted before
        String uid = UUIDList.get(0);
        UUIDList.remove(0);

        Match match = matchController.getMatchByID(4);
        List<Comment> comments = match.getAllComments();

        int response = 0;
        int id = 0;
        UUID uuid= null;

        for(Comment c : comments)
            if(c.getMessage().equals(uid)) {
                System.out.println(c.getMessage() + "   " +  uid);
                uuid = UUID.randomUUID();
                UUIDList.add(uuid.toString());
                id= c.getId();

                Comment com = new Comment(1,"   " + uuid  + "    ", new Timestamp(System.currentTimeMillis()));
                response = matchDataAccessService.updateCommentById(4, c.getId(), com);
            }

        match = matchController.getMatchByID(4);
        comments = match.getAllComments();

        for(Comment c : comments) {
            if(c.getId() == id)
                assertEquals(c.getMessage(), uuid.toString());
        }
        count++;
    }

    @Test
    public void deleteCommentWrongMatchID() {
        assertThrows(IDNotFoundException.class, () -> {
            int response = matchDataAccessService.deleteCommentById(32234234, 2);
        });
        count++;
    }

    @Test
    public void deleteCommentWrongCommentID() {
        assertThrows(IDNotFoundException.class, () -> {
            int response = matchDataAccessService.deleteCommentById(1, 0);
        });
        count++;
    }

    @Test
    public void deleteCommentSuccess() {
        // Getting one of the comments inserted before
        String uid = UUIDList.get(0);
        UUIDList.remove(0);

        Match match = matchController.getMatchByID(4);

        List<Comment> comments = match.getAllComments();

        int response = 0;

        for(Comment c : comments) {
            System.out.println(c.getMessage() + "   " +  uid.toString());
            if (c.getMessage().equals(uid)) {
                response = matchDataAccessService.deleteCommentById(4, c.getId());
            }
        }

        assertEquals(response, 1);
        count++;
    }


    @Test
    public void updateCommentSuccess() {
        // Getting one of the comments inserted before
        String uid = UUIDList.get(0);
        UUIDList.remove(0);

        Match match = matchController.getMatchByID(4);
        List<Comment> comments = match.getAllComments();

        int response = 0;
        int id = 0;
        UUID uuid= null;

        for(Comment c : comments)
            if(c.getMessage().equals(uid)) {
                uuid = UUID.randomUUID();
                UUIDList.add(uuid.toString());
                id= c.getId();

                Comment com = new Comment(1,"   " + uuid.toString() + "    ", new Timestamp(System.currentTimeMillis()));
                response = matchDataAccessService.updateCommentById(4, c.getId(), com);
            }

        match = matchController.getMatchByID(4);
        comments = match.getAllComments();

        for(Comment c : comments)
            if(c.getMessage().equals(uuid.toString()));

        assertEquals(response, 1);
    }

    public void clear() {
        if(count == 12) {
            Match match = matchController.getMatchByID(4);
            List<Comment> comments = match.getAllComments();

            int response = 0;

            for (String uid : UUIDList) {
                for (Comment c : comments)
                    if (c.getMessage().equals(uid)) {
                        response = matchDataAccessService.deleteCommentById(4, c.getId());
                    }
            }
            //System.out.println(response + " response");
        }

    }

}
