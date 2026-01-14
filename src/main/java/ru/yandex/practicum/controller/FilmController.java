package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.service.FilmService;
import ru.yandex.practicum.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public FilmController(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    // CRUDs of films
    @GetMapping
    public Collection<Film> findAll() {
        // calling
        log.info("* Calling *, class InMemoryFilmStorage, method findAll()");
        return filmService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film include(@Valid @RequestBody Film film) {

        //validation
        log.debug("* Validation * is starting, method include()");
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                || film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        log.debug("* Validation * is passed, method include()");

        // calling
        log.info("* Calling *, class InMemoryFilmStorage, method include()");
        return filmService.include(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {

        //validation
        log.debug("* Validation * is starting, method update()");
        log.info("Updating the film");
        if (film.getId() == null) {
            throw new NullPointerException("ID должен быть указан");
        }
        log.debug("* Validation * is passed, method update()");

        // calling
        log.info("* Calling *, class InMemoryFilmStorage, method update()");
        return filmService.update(film);
    }

    @DeleteMapping
    public Film delete(Film film) {
        // calling
        log.info("* Calling *, class InMemoryFilmStorage, method delete()");
        return filmService.delete(film);
    }

    // CRUDs of likes
    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {

        //validation
        log.debug("* Validation * is starting, method addLike()");
        if (filmService.findById(filmId) == null) {
            throw new NotFoundException("Film is not found");
        }

        if (userService.findById(userId) == null) {
            throw new NotFoundException("User is not found");
        }
        log.debug("* Validation * is passed, method addLike()");

        // calling
        log.info("* Calling *, class FilmService, method addLike()");
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {

        //validation
        log.debug("* Validation * is starting, method deleteLike()");
        if (filmService.findById(filmId) == null) {
            throw new NotFoundException("Film is not found");
        }

        if (userService.findById(userId) == null) {
            throw new NotFoundException("User is not found");
        }
        log.debug("* Validation * is passed, method deleteLike()");

        // calling
        log.info("* Calling *, class FilmService, method deleteLike()");
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getMostPopular(
            @Positive @RequestParam(defaultValue = "10") long count
    ) {
        // calling
        log.info("* Calling *, class FilmService, method getMostPopular()");
        return filmService.getMostPopular(count);
    }
}
