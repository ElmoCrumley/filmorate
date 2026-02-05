package ru.yandex.practicum.storage.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MotionPictureAA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Slf4j
public class FilmRowMapper implements RowMapper<Film> {
    private final Map<Long, Film> filmMap = new HashMap<>();

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.info("------------- * Start * FilmRowMapper * mapRow() -------------");

        long filmId = rs.getLong("id");

        Film film = filmMap.get(filmId);

        if (film == null) {
            film = new Film();
            film.setId(filmId);
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getTimestamp("releaseDate").toLocalDateTime().toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setGenres(new HashSet<>());
            filmMap.put(filmId, film);
        }

        Integer mpaId = rs.getObject("mpa_id", Integer.class);

        if (mpaId != null && film.getMpa() == null) {
            MotionPictureAA motionPictureAA = new MotionPictureAA();
            motionPictureAA.setId(mpaId);
            motionPictureAA.setName(rs.getString("mpa_name"));
            film.setMpa(motionPictureAA);
        }

        Integer genreId = rs.getObject("genre_id", Integer.class);

        if (genreId != null) {
            Genre genre = new Genre();
            genre.setId(genreId);
            genre.setName(rs.getString("genre_name"));
            film.getGenres().add(genre);  // Добавляем только уникальные жанры
        }

        log.info("------------- * return * ------------- {} \n", film);
        return film;
    }

    public Collection<Film> getFilms() {
        return filmMap.values();
    }
}
