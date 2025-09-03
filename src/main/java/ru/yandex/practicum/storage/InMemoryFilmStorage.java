package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> findAll() {
        log.info("Getting a list of films");
        return films.values();
    }

    @Override
    public Film include(@Valid @RequestBody Film film) {

        log.debug("Validation is starting");
        if (film.getDescription().length() > 200) {
            log.error("Exception, description's length is more that 200");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                || film.getReleaseDate().isAfter(LocalDate.now())) {
            log.error("Exception, the date of release is before December 28, 1895.");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        log.debug("Validation is passed");

        film.setId(getNextId());
        log.trace("Set an ID");
        films.put(film.getId(), film);
        log.trace("Put the film");
        return film;
    }

    @Override
    public Film update(@Valid @RequestBody Film film) {

        log.info("Updating the film");
        if (film.getId() == null) {
            log.error("Exception, ID is empty");
            throw new NullPointerException("ID должен быть указан");
        }

        Long filmId = film.getId();
        log.trace("Field filmId has been created");

        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            log.trace("Field oldFilm has been created");

            oldFilm.setReleaseDate(film.getReleaseDate());
            log.trace("Set the release date");
            oldFilm.setName(film.getName());
            log.trace("Set the name");
            oldFilm.setDuration(film.getDuration());
            log.trace("Set the duration");
            oldFilm.setDescription(film.getDescription());
            log.trace("Set the description");
            return oldFilm;
        }
        log.error("Film with this ID is not found");
        throw new NotFoundException("Фильм с ID = " + filmId + " не найден");
    }

    @Override
    public Film delete(@Valid @RequestBody Film film) {
        return new Film();
    }

    private long getNextId() {
        log.trace("Creating an ID");
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
