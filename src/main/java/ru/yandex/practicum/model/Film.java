package ru.yandex.practicum.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
}