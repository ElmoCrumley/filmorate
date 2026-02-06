package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Optional;

@RestController
@RequestMapping("/films")
@Validated
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public FilmController(@Qualifier("filmService") FilmService filmService,
                          @Qualifier("userService") UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    // CRUDs of films
    @GetMapping
    public Collection<Film> findAll() {
        // calling
        log.info("[Calling FilmController findAll()]");
        return filmService.findAll();
    }

    @GetMapping("/{filmId}")
    public Optional<Film> findById(@PathVariable("filmId") Long id) {
        log.info("[Calling FilmController findById()]");
        return filmService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film include(@Valid @RequestBody Film film) {
        log.debug("[Validation FilmController include()]");
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                || film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        // calling
        log.info("[Calling FilmController include()]");
        return filmService.include(film);
    }

    @PutMapping
    public Optional<Film> update(@Valid @RequestBody Film film) {
        log.debug("[Validation FilmController update()]");
        if (film.getId() == null) {
            throw new NullPointerException("ID должен быть указан");
        }

        // calling
        log.info("[Calling FilmController update()]");
        return filmService.update(film);
    }

    @DeleteMapping
    public Optional<Film> delete(Film film) {
        // calling
        log.info("[Calling FilmController delete()]");
        return filmService.delete(film);
    }

    // CRUDs of likes
    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {
        log.debug("[Validation FilmController addLike()]");
        if (filmService.findById(filmId).isEmpty()) {
            throw new NotFoundException("Film is not found");
        }

        if (userService.findById(userId).isEmpty()) {
            throw new NotFoundException("User is not found");
        }

        // calling
        log.info("[Calling FilmController addLike()]");
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId
    ) {
        log.debug("[Validation FilmController deleteLike()]");
        if (filmService.findById(filmId).isEmpty()) {
            throw new NotFoundException("Film is not found");
        }

        if (userService.findById(userId).isEmpty()) {
            throw new NotFoundException("User is not found");
        }

        // calling
        log.info("[Calling FilmController deleteLike()]");
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getMostPopular(
            @Positive @RequestParam(defaultValue = "10") long count
    ) {
        // calling
        log.info("[Calling FilmController getMostPopular()]");
        return filmService.getMostPopular(count);
    }
}

