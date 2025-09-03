package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.Film;

public interface FilmStorage {

    public Film include(@Valid @RequestBody Film film);

    public Film update(@Valid @RequestBody Film film);

    public Film delete(@Valid @RequestBody Film film);
}
