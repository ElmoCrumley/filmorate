package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
public class FilmService {
    final private FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    // reads
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long filmId) {
        return filmStorage.findById(filmId);
    }

    // other CRUDs
    public Film include(Film film) {
        return filmStorage.include(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film delete(Film film) {
        return filmStorage.delete(film);
    }

    // CRUDs of likes
    public void addLike(Long filmId, Long userId) {
        filmStorage.findLikesByFilmId(filmId).add(userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.findLikesByFilmId(filmId).remove(userId);
    }

    // Read populars
    public List<Film> getMostPopular(long count) {
        return filmStorage.getMostPopular(count);
    }
}
