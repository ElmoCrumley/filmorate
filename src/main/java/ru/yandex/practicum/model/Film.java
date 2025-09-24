package ru.yandex.practicum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@FieldDefaults
public class Film {
    Long id;
    @NotNull
    @NotBlank
    String name;
    String description;
    @NotNull
    LocalDate releaseDate;
    @Positive
    int duration;
    @JsonIgnore
    Set<Long> likes = new HashSet<>();
}