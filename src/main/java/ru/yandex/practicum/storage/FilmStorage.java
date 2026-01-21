package ru.yandex.practicum.storage;

import ru.yandex.practicum.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    // films CRUDs
    public Collection<Film> findAll();

    public Film findById(Long id);

    public Film include(Film film);

    public Film update(Film film);

    public Film delete(Film film);

    // CRUDs of likes
    public void addLike(Long filmId, Long userId);

    public void deleteLike(Long filmId, Long userId);

    // read populars
    public List<Film> getMostPopular(long count);

}
