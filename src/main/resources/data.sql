--DELETE FROM films_genre;
--DELETE FROM films_motion_picture_aa;
--DELETE FROM films;
--DELETE FROM users;
--DELETE FROM motion_picture_aa;
--DELETE FROM genre;
DELETE FROM commands;

INSERT INTO motion_picture_aa (id, name) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

INSERT INTO genre (id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

INSERT INTO commands (a,b,c,d,e,f,g) VALUES
('Table','field','field','field','field','field',null),
('users','id','email','login','name','birthday',null),
('friendshipRequests','user_id','requested_id',null,null,null,null),
('friendshipConfirmed','user_id','confirmed_friend_id',null,null,null,null),
('films','id','name','description','releaseDate','duration',null),
('users_likes','user_id','film_id',null,null,null,null),
('genre','id','name',null,null,null,null),
('films_genre','film_id','genre_id',null,null,null,null),
('motion_picture_aa','id','name',null,null,null,null),
('films_motion_picture_aa','film_id','motion_picture_aa_id',null,null,null,null),
('SELECT','FROM','LEFT JOIN','INSERT INTO ()','VALUES ()','DELETE FROM',null)

