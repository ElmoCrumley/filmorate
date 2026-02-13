package ru.yandex.practicum.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.SQLProblemException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MotionPictureAA;
import ru.yandex.practicum.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.storage.mappers.LikesMapper;
import ru.yandex.practicum.storage.mappers.MpasRowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

// DAO
@Repository("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final MpasRowMapper mpasRowMapper;
    private final LikesMapper likesMapper;
    // tables
    private static final String TABLE_FILMS = "films";
    private static final String TABLE_LIKES = "users_likes";
    private static final String TABLE_GENRE = "genre";
    private static final String TABLE_MOTION_PICTURE_ASSOCIATION = "motion_picture_aa";
    private static final String TABLE_FILMS_MPA = "films_motion_picture_aa";
    private static final String TABLE_FILMS_GENRE = "films_genre";
    // queries
    private static final String FIND_ALL_QUERY = "SELECT f.id, f.name, f.description, f.releaseDate, f.duration, " +
            "g.id AS genre_id, g.name AS genre_name, m.id AS mpa_id, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN films_motion_picture_aa fm ON f.id = fm.film_id " +
            "LEFT JOIN motion_picture_aa m ON fm.motion_picture_aa_id = m.id " +
            "LEFT JOIN films_genre fg ON f.id = fg.film_id " +
            "LEFT JOIN genre g ON fg.genre_id = g.id ";
    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genre";
    private static final String FIND_ALL_MPAS_QUERY = "SELECT * FROM motion_picture_aa";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_FILMS_MPA_QUERY = "select mpaa.id as id, mpaa.name as name " +
            "from films_motion_picture_aa fmpaa " +
            "left join motion_picture_aa mpaa on fmpaa.motion_picture_aa_id = mpaa.id " +
            "where fmpaa.film_id = ?";
    private static final String FIND_FILMS_GENRES_QUERY = "select g.id as id, g.name as name " +
            "from films_genre fg " +
            "left join genre g on fg.genre_id = g.id " +
            "where fg.film_id = ?";
    private static final String FIND_FILMS_LIKES_QUERY = "select ul.user_id as id " +
            "from users_likes ul where ul.film_id = ?";
    private static final String FIND_MPA_NAME_QUERY = "SELECT name FROM " + TABLE_MOTION_PICTURE_ASSOCIATION +
            " WHERE id = ?";
    private static final String FIND_BY_ID_GENRE_QUERY = "SELECT * FROM genre WHERE id = ?";
    private static final String FIND_BY_ID_MPAS_QUERY = "SELECT * FROM motion_picture_aa WHERE id = ?";
    private static final String FIND_GENRES_NAME_QUERY = "SELECT name FROM " + TABLE_GENRE + " WHERE id = ?";
    private static final String FIND_MOST_POPULAR_QUERY = "SELECT f.id, f.name, f.description, f.releaseDate, " +
            "f.duration FROM films f " +
            "WHERE f.id IN (SELECT film_id FROM users_likes GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT ?) " +
            "ORDER BY (SELECT COUNT(user_id) FROM users_likes ul WHERE ul.film_id = f.id) DESC";
    private static final String INSERT_FILM_QUERY = "INSERT INTO " + TABLE_FILMS +
            " (name, description, releaseDate, duration) VALUES (?, ?, ?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO " + TABLE_LIKES + " (film_id, user_id) VALUES (?, ?)";
    private static final String INSERT_FILMS_MPA_QUERY = "INSERT INTO " + TABLE_FILMS_MPA +
            " (film_id, motion_picture_aa_id) VALUES (?, ?)";
    private static final String INSERT_FILMS_GENRE_QUERY = "INSERT INTO " + TABLE_FILMS_GENRE +
            " (film_id, genre_id) " +
            "VALUES (?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE " + TABLE_FILMS +
            " set name = ?, description = ?, releaseDate = ?, duration = ? where id = ?";
    private static final String UPDATE_MPA_QUERY = "UPDATE " + TABLE_FILMS_MPA +
            " set motion_picture_aa_id = ? where film_id = ?";
    private static final String UPDATE_GENRES_QUERY = "UPDATE " + TABLE_FILMS_GENRE +
            " set genre_id = ? where film_id = ?";
    private static final String DELETE_QUERY = "delete from " + TABLE_FILMS + " where id = ?";
    private static final String DELETE_LIKE_QUERY = "delete  from users_likes where film_id = ? AND user_id = ?";

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc,
                         GenreRowMapper genreRowMapper,
                         MpasRowMapper mpasRowMapper,
                         FilmRowMapper filmRowMapper,
                         LikesMapper likesmapper) {
        this.jdbc = jdbc;
        this.genreRowMapper = genreRowMapper;
        this.mpasRowMapper = mpasRowMapper;
        this.filmRowMapper = filmRowMapper;
        this.likesMapper = likesmapper;
    }

    // films CRUDs
    @Override
    public Collection<Film> findAll() {
        log.info("------------- * Start / Finish * FilmDbStorage * findAll() -------------");

        FilmRowMapper filmRowMapper = new FilmRowMapper();

        List<Film> films = jdbc.query(FIND_ALL_QUERY, filmRowMapper);

        if (films.isEmpty()) {
            throw new NotFoundException("There are no data records in the table");
        }

        for (Film film : films) {
            log.info("* FilmDbStorage * findAll() {} ", film);
        }

        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        log.info("------------- * Start / Finish * FilmDbStorage * findById() -------------");

        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, filmRowMapper, id);

            if (film != null && film.getId() != null) {
                try {
                    MotionPictureAA mpa = jdbc.queryForObject(FIND_FILMS_MPA_QUERY, mpasRowMapper, id);

                    film.setMpa(mpa);
                } catch (EmptyResultDataAccessException e) {
                    film.setMpa(null);
                }

                Set<Genre> genres = new HashSet<>(jdbc.query(FIND_FILMS_GENRES_QUERY, genreRowMapper, id));

                film.setGenres(genres);

                Set<Long> likes = new HashSet<>(jdbc.query(FIND_FILMS_LIKES_QUERY, likesMapper, id));

                film.setLikes(likes);
                return Optional.of(film);
            } else {
                return Optional.empty();
            }
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film include(Film film) {
        log.info("------------- * Start * FilmDbStorage * include() ------------");
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_FILM_QUERY, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                LocalDateTime releaseDate = film.getReleaseDate().atStartOfDay();
                ps.setObject(3, releaseDate);
                ps.setInt(4, film.getDuration());
                return ps;
            }, keyHolder);
        } catch (DataAccessException e) {
            throw new SQLProblemException("include film failed");
        }

        Long filmId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;

        if (filmId == null) {
            throw new NotFoundException("id is not found");
        }

        film.setId(filmId);

        if (film.getMpa() != null) {
            Integer id = film.getMpa().getId();
            if (id != null) {
                Integer existingMpaCount = jdbc.queryForObject("select count(*) " +
                        "from motion_picture_aa where id = ?", Integer.class, id);
                if (existingMpaCount != null && existingMpaCount > 0) {
                    try {
                        jdbc.update(INSERT_FILMS_MPA_QUERY, filmId, film.getMpa().getId());
                    } catch (EmptyResultDataAccessException e) {
                        throw new SQLProblemException("problem in INSERT_FILMS_MPA_QUERY");
                    }
                } else {
                    throw new NotFoundException("mpa is not found");
                }
            }
        }

        if (!film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Integer id = genre.getId();
                if (id != null) {
                    Integer existingGenreCount = jdbc.queryForObject("select count(*) " +
                            "from genre where id = ?", Integer.class, id);
                    if (existingGenreCount != null && existingGenreCount > 0) {
                        try {
                            jdbc.update(INSERT_FILMS_GENRE_QUERY, filmId, genre.getId());
                        } catch (EmptyResultDataAccessException e) {
                            throw new SQLProblemException("problem in INSERT_FILMS_GENRE_QUERY");
                        }
                    } else {
                        throw new NotFoundException("genre is not found");
                    }
                }
            }
        }

        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        log.info("------------- * Start * FilmDbStorage * update() -------------");
        int rowsAffected = jdbc.update(connection -> {
            // film
            log.info("* Info * FilmDbStorage, method update(), film = {}", film);

            PreparedStatement stmtFindFilm = connection.prepareStatement(FIND_BY_ID_QUERY);
            log.info("* Info * FilmDbStorage, method update(), PreparedStatement ready");
            stmtFindFilm.setLong(1, film.getId());

            ResultSet rsFind = stmtFindFilm.executeQuery();
            log.info("* Info * FilmDbStorage, method update(), ResultSet ready");
            if (!rsFind.next()) {
                throw new NotFoundException("Film`s id is not found");
            }

            log.info("* Info * FIND_BY_ID_QUERY is complete");

            PreparedStatement stmtUpdateFilm = connection.prepareStatement(UPDATE_FILM_QUERY);

            stmtUpdateFilm.setString(1, film.getName());
            stmtUpdateFilm.setString(2, film.getDescription());

            LocalDateTime releaseDate = film.getReleaseDate().atStartOfDay();

            stmtUpdateFilm.setObject(3, releaseDate);
            stmtUpdateFilm.setInt(4, film.getDuration());
            stmtUpdateFilm.setLong(5, film.getId());

            int rowsUpdated = stmtUpdateFilm.executeUpdate();

            // mpa
            if (film.getMpa() != null) {
                int mpaId = film.getMpa().getId();

                MotionPictureAA mpa = film.getMpa();

                log.info("* Info * FilmDbStorage, method update(), mpa = {}", mpa);

                PreparedStatement stmtFindMpa = connection.prepareStatement(FIND_MPA_NAME_QUERY);

                stmtFindMpa.setInt(1, mpaId);

                ResultSet rs = stmtFindMpa.executeQuery();

                if (rs.next()) {
                    mpa.setName(rs.getString("name"));
                } else {
                    throw new NotFoundException("Mpa`s id is not found");
                }

                PreparedStatement stmtUpdateMpa = connection.prepareStatement(UPDATE_MPA_QUERY);

                stmtUpdateMpa.setInt(1, mpaId);
                stmtUpdateMpa.setLong(2, film.getId());

                int totalRowUpdated = stmtUpdateMpa.executeUpdate();

                if (totalRowUpdated == 0) {
                    throw new SQLException("Данные не обновлены");
                }
            }

            // genres
            Set<Genre> genres = film.getGenres();

            log.info("* Info * FilmDbStorage, method update(), genres = {}", film.getGenres());

            PreparedStatement stmtFindGenre = connection.prepareStatement(FIND_GENRES_NAME_QUERY);
            PreparedStatement stmtUpdateGenres = connection.prepareStatement(UPDATE_GENRES_QUERY);

            for (Genre genre : genres) {
                stmtFindGenre.setInt(1, genre.getId());

                ResultSet rs = stmtFindGenre.executeQuery();

                if (rs.next()) {
                    genre.setName(rs.getString("name"));
                } else {
                    throw new NotFoundException("Genre id is not found");
                }

                stmtUpdateGenres.setLong(1, film.getId());
                stmtUpdateGenres.setInt(2, genre.getId());
                int totalRowsUpdated = stmtUpdateGenres.executeUpdate();

                if (totalRowsUpdated == 0) {
                    throw new SQLException("Данные не обновлены");
                }
            }

            return stmtUpdateFilm;
        });

        if (rowsAffected == 0) {
            throw new NotFoundException("Id is not found");
        }

        log.info("* Info * FilmDbStorage, method update(), return film = {}", film);
        log.info("------------- * Finish * FilmDbStorage * update() -------------");
        return Optional.of(film);
    }

    @Override
    public Optional<Film> delete(Film film) {
        int rowsAffected = jdbc.update(DELETE_QUERY, film.getId());

        if (rowsAffected == 0) {
            throw new RuntimeException("Данные не удалены");
        }

        return Optional.of(film);
    }

    // CRUDs of likes
    @Override
    public void addLike(Long filmId, Long userId) {
        int rowsAffected = jdbc.update(INSERT_LIKE_QUERY, filmId, userId);

        if (rowsAffected == 0) {
            throw new RuntimeException("Лайк не добавлен");
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        int rowsAffected = jdbc.update(DELETE_LIKE_QUERY, filmId, userId);

        if (rowsAffected == 0) {
            throw new RuntimeException("Лайк не удалён");
        }
    }

    // read populars
    @Override
    public List<Film> getMostPopular(long count) {
        log.info("------------- * Start / Finish * FilmDbStorage * getMostPopular() -------------");
        List<Film> films;

        try {
            films = jdbc.query(FIND_MOST_POPULAR_QUERY, filmRowMapper, count);
            for (Film film : films) {
                if (film != null) {
                    long filmId = film.getId();

                    try {
                        MotionPictureAA mpa = jdbc.queryForObject(FIND_FILMS_MPA_QUERY, mpasRowMapper, filmId);

                        film.setMpa(mpa);
                    } catch (EmptyResultDataAccessException e) {
                        film.setMpa(null);
                    }

                    Set<Genre> genres = new HashSet<>(jdbc.query(FIND_FILMS_GENRES_QUERY, genreRowMapper, filmId));

                    film.setGenres(genres);

                    Set<Long> likes = new HashSet<>(jdbc.query(FIND_FILMS_LIKES_QUERY, likesMapper, filmId));

                    film.setLikes(likes);
                }
            }

        } catch (DataAccessException e) {
            throw new NotFoundException("Films not found");
        }
        return films;
    }

    // genres
    @Override
    public Collection<Genre> findAllGenres() {
        log.info("------------- * Start / Finish * FilmDbStorage * findAllGenres() -------------");

        GenreRowMapper genreRowMapper = new GenreRowMapper();

        List<Genre> genres = jdbc.query(FIND_ALL_GENRES_QUERY, genreRowMapper);

        if (genres.isEmpty()) {
            throw new NotFoundException("There are no data records in the table");
        }

        for (Genre genre : genres) {
            log.info("* FilmDbStorage * findAllGenres() {} ", genre);
        }

        return genres;
    }

    @Override
    public Optional<Genre> findGenreById(Integer genreId) {
        log.info("------------- * Start / Finish * FilmDbStorage * findGenreById() -------------");

        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID_GENRE_QUERY, genreRowMapper, genreId));
        } catch (DataAccessException e) {
            throw new NotFoundException("not found");
        } catch (Throwable e) {
            throw new NotFoundException("search failed");
        }
    }

    // mpa's
    @Override
    public Collection<MotionPictureAA> findAllMPAs() {
        log.info("------------- * Start / Finish * FilmDbStorage * findAllMPAs() -------------");

        MpasRowMapper mpasRowMapper = new MpasRowMapper();

        List<MotionPictureAA> listOfMpa = jdbc.query(FIND_ALL_MPAS_QUERY, mpasRowMapper);

        if (listOfMpa.isEmpty()) {
            throw new NotFoundException("There are no data records in the table");
        }

        for (MotionPictureAA motionPictureAA : listOfMpa) {
            log.info("* FilmDbStorage * findAllMPAs() {} ", motionPictureAA);
        }

        return listOfMpa;
    }

    @Override
    public Optional<MotionPictureAA> findMPAById(Integer mpaId) {
        log.info("------------- * Start / Finish * FilmDbStorage * findMPAById() -------------");

        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID_MPAS_QUERY, mpasRowMapper, mpaId));
        } catch (DataAccessException e) {
            throw new NotFoundException("not found");
        } catch (Throwable e) {
            throw new NotFoundException("search failed");
        }
    }
}