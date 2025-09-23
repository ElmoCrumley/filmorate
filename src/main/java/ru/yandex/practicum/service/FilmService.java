package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.storage.InMemoryFilmStorage;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@Service
public class FilmService {

    public InMemoryFilmStorage inMemoryFilmStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    public void addLike(
            Long id,
            Long userId
    ) {
        inMemoryFilmStorage.getFilms().get(id).getLikes().add(userId);
    }

    public void deleteLike(
            Long id,
            Long userId
    ) {
        inMemoryFilmStorage.getFilms().get(id).getLikes().remove(userId);
    }

    public List<Film> getMostPopular(
        long count
    ) {
        return inMemoryFilmStorage.findAll()
                .stream()
                .sorted(new LikesComparator())
                .limit(count)
                .toList();
    }

    public static class LikesComparator implements Comparator<Film> {
        @Override
        public int compare(Film f1, Film f2) {
            return Integer.compare(f2.getLikes().size(), f1.getLikes().size());
        }
    }
}
