package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.service.FilmService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/genres")
@Validated
@Slf4j
public class GenresController {
    private final FilmService filmService;

    @Autowired
    public GenresController(@Qualifier("filmService") FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Genre> findAllGenres() {
        log.info("[Calling GenresController findAllGenres()]");
        return filmService.findAllGenres();
    }

    @GetMapping("/{genreId}")
    public Optional<Genre> findGenreById(@PathVariable("genreId") int genreId) {
        log.info("[Calling GenresController findGenreById()]");
        if (genreId < 0 || genreId > 6) {
            throw new NotFoundException("not in range 1 to 6");
        }

        return filmService.findGenreById(genreId);
    }
}
