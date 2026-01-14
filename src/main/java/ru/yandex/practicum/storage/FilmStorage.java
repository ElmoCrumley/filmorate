package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FilmStorage {

    // finds
    public Collection<Film> findAll();

    public Film findById(Long id);

    public Set<Long> findLikesByFilmId(Long userId);

    // other calls
    public Film include(@Valid @RequestBody Film film);

    public Film update(@Valid @RequestBody Film film);

    public Film delete(@Valid @RequestBody Film film);

    public List<Film> getMostPopular(long count);

}
