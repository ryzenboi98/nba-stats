create table Comments (
    comment_id int NOT NULL,
    match_id int NOT NULL,
    comment VARCHAR(200) NOT NULL,
    c_date timestamp NOT NULL,
    primary key(comment_id, match_id)
);