package ru.yandex.practicum.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    Long id;
    @Email
    String email;
    @NotNull
    String login;
    String name;
    @NotNull
    LocalDate birthday;
}