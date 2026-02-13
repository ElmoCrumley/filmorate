package ru.yandex.practicum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Long id;
    @Email
    @NotNull
    @NotBlank
    String email;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^\\S*$", message = "Логин не может содержать пробелы")
    String login;
    String name;
    @NotNull
    @Past(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;
    @JsonIgnore
    Set<Long> friendshipRequests = new HashSet<>();
    Set<Long> friendshipConfirmed = new HashSet<>();
}