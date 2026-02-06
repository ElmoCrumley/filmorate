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
import ru.yandex.practicum.model.MotionPictureAA;
import ru.yandex.practicum.service.FilmService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/mpa")
@Validated
@Slf4j
public class MPAController {
    private final FilmService filmService;

    @Autowired
    public MPAController(@Qualifier("filmService") FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<MotionPictureAA> findAllMPAs() {
        log.info("[Calling MPAController findAll()]");
        return filmService.findAllMPAs();
    }

    @GetMapping("/{mpaId}")
    public Optional<MotionPictureAA> findMPAById(@PathVariable("mpaId") Integer mpaId) {
        log.info("[Calling MPAController findMPAById()]");
        if (mpaId < 0 || mpaId > 5) {
            throw new NotFoundException("not in range 1 to 5");
        }

        return filmService.findMPAById(mpaId);
    }
}
