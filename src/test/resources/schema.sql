CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(40) NOT NULL,
    name VARCHAR(40),
    birthday TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS friendshipRequests (
    user_id BIGINT NOT NULL REFERENCES users(id),
    requested_id BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (user_id, requested_id)
);

CREATE TABLE IF NOT EXISTS friendshipConfirmed (
    user_id BIGINT NOT NULL REFERENCES users(id),
    confirmed_friend_id BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (user_id, confirmed_friend_id)
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(1024) NOT NULL,
    description VARCHAR(1024),
    releaseDate TIMESTAMP NOT NULL,
    duration INT NOT NULL
);

CREATE TABLE IF NOT EXISTS users_likes (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS genre (
    id INT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films_genre (
    film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    genre_id INT NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS motion_picture_aa (
    id INT UNIQUE NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS films_motion_picture_aa (
    film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    motion_picture_aa_id INT NOT NULL REFERENCES motion_picture_aa(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, motion_picture_aa_id)
);
