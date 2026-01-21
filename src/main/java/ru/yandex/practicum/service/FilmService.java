package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.storage.FilmStorage;

import java.util.Collection;
import java.util.List;

@Service("filmService")
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    // films CRUDs
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long filmId) {
        return filmStorage.findById(filmId);
    }

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
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.deleteLike(filmId, userId);
    }

    // read populars
    public List<Film> getMostPopular(long count) {
        return filmStorage.getMostPopular(count);
    }
}
