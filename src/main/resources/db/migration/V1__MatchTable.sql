create table Matches (
    match_id int not null primary key,
    home_team varchar(50) not null,
    visitor_team varchar(50) not null,
    home_score int not null,
    visitor_score int not null,
    m_date timestamp not null
);

create table Comments (
    comment_id int NOT NULL,
    match_id int,
    comment VARCHAR(200) NOT NULL,
    c_date timestamp NOT NULL,
    primary key(comment_id, match_id),
    foreign key (match_id) references Matches(match_id)
);