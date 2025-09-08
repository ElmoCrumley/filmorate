package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.storage.InMemoryFilmStorage;

import java.util.Comparator;
import java.util.List;

@RestController
@Service
public class FilmService {

    InMemoryFilmStorage filmStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void addLike(
            @PathVariable ("id") Long id,
            @PathVariable ("userId") Long userId
    ) {
        filmStorage.getFilms().get(id).getLikes().add(userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void deleteLike(
            @PathVariable ("id") Long id,
            @PathVariable ("userId") Long userId
    ) {
        filmStorage.getFilms().get(id).getLikes().remove(userId);
    }

    @GetMapping("/films/popular?count={count}")
    public List<Film> getMostPopular(
        @RequestParam (defaultValue = "10") int count
    ) {
        return filmStorage.getFilms().values()
                .stream()
                .sorted(new LikesComparator())
                .limit(count)
                .toList();
    }

    public static class LikesComparator implements Comparator<Film> {
        @Override
        public int compare(Film f1, Film f2) {
            return Integer.compare(f1.getLikes().size(), f2.getLikes().size());
        }
    }
}
