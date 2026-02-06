package ru.yandex.practicum.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MotionPictureAA;
import ru.yandex.practicum.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.storage.mappers.GenreRowMapper;
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
    private static final String FIND_BY_ID_QUERY = "SELECT f.id, f.name, f.description, f.releaseDate, f.duration, " +
            "g.id AS genre_id, g.name AS genre_name, m.id AS mpa_id, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN films_motion_picture_aa fm ON f.id = fm.film_id " +
            "LEFT JOIN motion_picture_aa m ON fm.motion_picture_aa_id = m.id " +
            "LEFT JOIN films_genre fg ON f.id = fg.film_id " +
            "LEFT JOIN genre g ON fg.genre_id = g.id " +
            "WHERE f.id = ?";
    private static final String FIND_MPA_NAME_QUERY = "SELECT name FROM " + TABLE_MOTION_PICTURE_ASSOCIATION +
            " WHERE id = ?";
    private static final String FIND_BY_ID_GENRE_QUERY = "SELECT * FROM genre WHERE id = ?";
    private static final String FIND_BY_ID_MPAS_QUERY = "SELECT * FROM motion_picture_aa WHERE id = ?";
    private static final String FIND_GENRES_NAME_QUERY = "SELECT name FROM " + TABLE_GENRE + " WHERE id = ?";
    private static final String FIND_MOST_POPULAR_QUERY = "SELECT f.id, f.name, f.description, f.releaseDate, " +
            "f.duration, g.id AS genre_id, g.name AS genre_name, m.id AS mpa_id, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN films_motion_picture_aa fm ON f.id = fm.film_id " +
            "LEFT JOIN motion_picture_aa m ON fm.motion_picture_aa_id = m.id " +
            "LEFT JOIN films_genre fg ON f.id = fg.film_id " +
            "LEFT JOIN genre g ON fg.genre_id = g.id " +
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
                         FilmRowMapper filmRowMapper,
                         GenreRowMapper genreRowMapper,
                         MpasRowMapper mpasRowMapper) {
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
        this.mpasRowMapper = mpasRowMapper;
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
            List<Film> films = jdbc.query(FIND_BY_ID_QUERY, filmRowMapper, id);

            if (films.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(films.get(0));
            }
        } catch (DataAccessException e) {
            throw new NotFoundException("film is not found");
        }
    }

    @Override
    public Film include(Film film) {
        log.info("------------- * Start * FilmDbStorage * include() ------------");
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            // film
            PreparedStatement stmtInsertFilm = connection.prepareStatement(INSERT_FILM_QUERY,
                    Statement.RETURN_GENERATED_KEYS);

            stmtInsertFilm.setString(1, film.getName());
            stmtInsertFilm.setString(2, film.getDescription());

            LocalDateTime releaseDate = film.getReleaseDate().atStartOfDay();

            stmtInsertFilm.setObject(3, releaseDate);
            stmtInsertFilm.setInt(4, film.getDuration());
            stmtInsertFilm.executeUpdate();

            ResultSet generatedKeys = stmtInsertFilm.getGeneratedKeys();

            if (generatedKeys.next()) {
                Long generatedId = generatedKeys.getLong(1);
                film.setId(generatedId);
            }

            log.info("* Info * FilmDbStorage * include() --- stmtInsertFilm --- {}", film);

            // mpa
            if (film.getMpa() != null) {
                int mpaId = film.getMpa().getId();

                MotionPictureAA mpa = film.getMpa();

                log.info("* SQL * FilmDbStorage * include() --- stmtFindMpa --- {}", mpa);

                PreparedStatement stmtFindMpa = connection.prepareStatement(FIND_MPA_NAME_QUERY);

                stmtFindMpa.setInt(1, mpaId);

                ResultSet rs = stmtFindMpa.executeQuery();

                if (rs.next()) {
                    mpa.setName(rs.getString("name"));
                } else {
                    throw new NotFoundException("Mpa`s id is not found");
                }

                log.info("* SQL * FilmDbStorage * include() --- stmtInsertMpa --- {}", film);

                PreparedStatement stmtInsertMpa = connection.prepareStatement(INSERT_FILMS_MPA_QUERY);

                stmtInsertMpa.setLong(1, film.getId());
                stmtInsertMpa.setInt(2, mpaId);
                stmtInsertMpa.executeUpdate();
                log.info("* Info * FilmDbStorage * include() -- stmtInsertMpa {}", mpa);
            }

            // genres
            if (film.getGenres().isEmpty()) {
                film.setGenres(new HashSet<>());
            } else {
                Set<Genre> genres = film.getGenres();

                log.info("* SQL * FilmDbStorage * include() --- stmtFindGenres --- {}", genres);

                PreparedStatement stmtFindGenre = connection.prepareStatement(FIND_GENRES_NAME_QUERY);
                PreparedStatement stmtInsertGenre = connection.prepareStatement(INSERT_FILMS_GENRE_QUERY);

                for (Genre genre : genres) {
                    stmtFindGenre.setInt(1, genre.getId());

                    ResultSet rs = stmtFindGenre.executeQuery();

                    if (rs.next()) {
                        genre.setName(rs.getString("name"));
                    } else {
                        throw new NotFoundException("Genre id is not found");
                    }

                    stmtInsertGenre.setLong(1, film.getId());
                    stmtInsertGenre.setInt(2, genre.getId());
                    stmtInsertGenre.executeUpdate();
                }

                log.info("* Info * FilmDbStorage, method include(), Genres = {}", genres);
            }

            return stmtInsertFilm;
        }, keyHolder);

        Long generatedId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;

        if (generatedId == null) {
            throw new NotFoundException(" FilmDbStorage, include(), id = null");
        }

        log.info("------------- * Finish * FilmDbStorage * include() -------------");
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

//            int totalRowsUpdated = 0;

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
        return jdbc.query(FIND_MOST_POPULAR_QUERY, filmRowMapper, count);
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