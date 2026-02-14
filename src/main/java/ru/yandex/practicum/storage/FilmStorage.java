package ru.yandex.practicum.storage;

import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MotionPictureAA;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    // films CRUDs
    public Collection<Film> findAll();

    public Optional<Film> findById(Long id);

    public Film include(Film film);

    public Optional<Film> update(Film film);

    public Optional<Film> delete(Film film);

    // CRUDs of likes
    public void addLike(Long filmId, Long userId);

    public void deleteLike(Long filmId, Long userId);

    // read populars
    public List<Film> getMostPopular(long count);

    // genres
    Collection<Genre> findAllGenres();

    Optional<Genre> findGenreById(Integer genreId);

    // mpa's
    Collection<MotionPictureAA> findAllMPAs();

    Optional<MotionPictureAA> findMPAById(Integer mpaId);
}
