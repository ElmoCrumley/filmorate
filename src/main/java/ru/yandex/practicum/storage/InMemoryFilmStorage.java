package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Getter
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    public InMemoryUserStorage inMemoryUserStorage;

    @Autowired
    InMemoryFilmStorage(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public Collection<Film> findAll() {
        log.trace("method * findAll(), Getting a list of films");
        return films.values();
    }

    @Override
    public Film include(@Valid @RequestBody Film film) {
        film.setId(getNextId());
        log.trace("method * include(), Set an ID");
        films.put(film.getId(), film);
        log.trace("method * include(), Put the film");
        return film;
    }

    @Override
    public Film update(@Valid @RequestBody Film film) {
        Long filmId = film.getId();
        log.trace("method * update(), Field filmId has been created");

        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            log.trace("method * update(), Field oldFilm has been created");

            oldFilm.setReleaseDate(film.getReleaseDate());
            log.trace("method * update(), Set the release date");
            oldFilm.setTitle(film.getTitle());
            log.trace("method * update(), Set the name");
            oldFilm.setDuration(film.getDuration());
            log.trace("method * update(), Set the duration");
            oldFilm.setDescription(film.getDescription());
            log.trace("method * update(), Set the description");
            return oldFilm;
        }

        log.error("method * update(), Film with this ID is not found");
        throw new NotFoundException("Фильм с ID = " + filmId + " не найден");
    }

    @Override
    public Film delete(@Valid @RequestBody Film film) {
        films.remove(film.getId());
        return new Film();
    }

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
