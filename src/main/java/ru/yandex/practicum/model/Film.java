package ru.yandex.practicum.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Positive
    private int duration;
}