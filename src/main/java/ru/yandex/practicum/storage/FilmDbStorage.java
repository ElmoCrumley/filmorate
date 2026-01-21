package ru.yandex.practicum.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

// DAO
@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    // tables
    private static final String TABLE_FILMS = "films";
    private static final String TABLE_LIKES = "likes";
    private static final String TABLE_GENRE = "genre";
    private static final String TABLE_MOTION_PICTURE_ASSOCIATION = "motionPictureAssociation";
    // queries
    private static final String FIND_ALL_QUERY = "SELECT * FROM " + TABLE_FILMS;
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM " + TABLE_FILMS + " WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO " +
            "films(title, description, releaseDate, duration, motionPictureAssociation) " +
            "VALUES (?, ?, ?, ?, ?) returning id";
    private static final String UPDATE_QUERY = "UPDATE " + TABLE_FILMS +
            " set title = ?, description = ?, releaseDate = ?, duration = ? motionPictureAssociation = ? where id = ?";
    private static final String DELETE_QUERY = "delete from " + TABLE_FILMS + " where id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "delete from " + TABLE_FILMS + " where film_id = ? AND user_id = ?";
    private static final String GET_MOST_POPULAR_QUERY = "SELECT *" +
            "FROM " + TABLE_FILMS +
            " WHERE film_id IN (" +
            "    SELECT film_id" +
            "    FROM " + TABLE_LIKES +
            "    GROUP BY film_id" +
            "    ORDER BY COUNT(user_id) DESC" +
            "    LIMIT ?" +
            " )";

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper filmRowMapper) {
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
    }

    // films CRUDs
    @Override
    public Collection<Film> findAll() {
        return jdbc.query(FIND_ALL_QUERY, filmRowMapper);
    }

    @Override
    public Film findById(Long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, filmRowMapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public Film include(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY, new String[]{"id"});
            stmt.setString(1, film.getTitle());
            stmt.setString(2, film.getDescription());
            stmt.setObject(3, film.getReleaseDate());
            stmt.setInt(4, film.getDuration());
            stmt.setString(5, film.getMotionPictureAssociation());
            return stmt;
        }, keyHolder);

        Long generatedId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;

        if (generatedId == null) {
            throw new NotFoundException(" FilmDbStorage, include(), id = null");
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        jdbc.update(UPDATE_QUERY,
                film.getTitle(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMotionPictureAssociation());
        return film;
    }

    @Override
    public Film delete(Film film) {
        jdbc.update(DELETE_QUERY, film.getId());
        return film;
    }

    // CRUDs of likes
    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    // read populars
    @Override
    public List<Film> getMostPopular(long count) {
        return jdbc.query(GET_MOST_POPULAR_QUERY, filmRowMapper, count);
    }
}