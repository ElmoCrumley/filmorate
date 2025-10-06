package ru.yandex.practicum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    String login;
    String name;
    @NotNull
    LocalDate birthday;
    @JsonIgnore
    Set<Long> friendshipRequests = new HashSet<>();
    @JsonIgnore
    Set<Long> friendshipConfirmed = new HashSet<>();
}