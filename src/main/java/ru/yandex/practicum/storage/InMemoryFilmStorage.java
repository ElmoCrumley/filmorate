package ru.yandex.practicum.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MotionPictureAA;

import java.util.*;

@Component
@Getter
@Slf4j
@Qualifier("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    // films CRUDs
    @Override
    public Collection<Film> findAll() {
        log.trace("method * findAll(), Getting a list of films");
        return List.copyOf(films.values());
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        return Optional.of(films.get(filmId));
    }

    @Override
    public Film include(Film film) {
        film.setId(getNextId());
        log.trace("method * include(), Set an ID");
        films.put(film.getId(), film);
        log.trace("method * include(), Put the film");
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        Long filmId = film.getId();
        log.trace("method * update(), Field filmId has been created");

        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            log.trace("method * update(), Field oldFilm has been created");

            oldFilm.setReleaseDate(film.getReleaseDate());
            log.trace("method * update(), Set the release date");
            oldFilm.setName(film.getName());
            log.trace("method * update(), Set the name");
            oldFilm.setDuration(film.getDuration());
            log.trace("method * update(), Set the duration");
            oldFilm.setDescription(film.getDescription());
            log.trace("method * update(), Set the description");
            return Optional.of(oldFilm);
        }

        log.error("method * update(), Film with this ID is not found");
        throw new NotFoundException("Фильм с ID = " + filmId + " не найден");
    }

    @Override
    public Optional<Film> delete(Film film) {
        films.remove(film.getId());
        return Optional.of(new Film());
    }

    // CRUDs of likes
    public Set<Long> findLikesByFilmId(Long filmId) {
        return films.get(filmId).getLikes();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        findLikesByFilmId(filmId).add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        findLikesByFilmId(filmId).remove(userId);
    }

    // read populars
    @Override
    public List<Film> getMostPopular(long count) {
        return  findAll()
                .stream()
                .sorted(new LikesComparator())
                .limit(count)
                .toList();
    }

    // genres
    @Override
    public Collection<Genre> findAllGenres() {
        return List.of();
    }

    @Override
    public Optional<Genre> findGenreById(Integer genreId) {
        return Optional.empty();
    }

    // mpa's
    @Override
    public Collection<MotionPictureAA> findAllMPAs() {
        return List.of();
    }

    @Override
    public Optional<MotionPictureAA> findMPAById(Integer mpaId) {
        return Optional.empty();
    }

    public static class LikesComparator implements Comparator<Film> {
        @Override
        public int compare(Film f1, Film f2) {
            return Integer.compare(f2.getLikes().size(), f1.getLikes().size());
        }
    }

    // create next ID
    private long getNextId() {
        log.trace("method * getNextId(), Creating an ID");
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
