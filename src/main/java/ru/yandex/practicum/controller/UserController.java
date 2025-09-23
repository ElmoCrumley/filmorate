package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.inMemoryUserStorage.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User create(@Valid @RequestBody User user) {

        log.debug("Validation is starting");
        if (user.getLogin().contains(" ")) {
            log.error("Exception, name is contains \" \"");
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("User changed name to login");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Exception, birthday is in the future");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Validation is passed");

        return userService.inMemoryUserStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {

        log.info("Updating the user");
        if (user.getId() == null) {
            log.error("Exception, ID is empty");
            throw new NullPointerException("Id должен быть указан");
        }

        return userService.inMemoryUserStorage.update(user);
    }

    @DeleteMapping
    public void delete(@Valid @RequestBody User user) {
        userService.inMemoryUserStorage.delete(user);
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) {

        if (userService.inMemoryUserStorage.getUsers().get(id) == null) {
            throw new NotFoundException("User is not found");
        }

        if (userService.inMemoryUserStorage.getUsers().get(friendId) == null) {
            throw new NotFoundException("Friend is not found");
        }

        log.info("Adding the friend {} for user {}", friendId, id);
        userService.addFriend(id, friendId);
        log.info("Users' {} friends: {}", id, userService.getFriends(id));
        log.info("Users' {} friends: {}", friendId, userService.getFriends(friendId));
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) {

        if (userService.inMemoryUserStorage.getUsers().get(id) == null) {
            throw new NotFoundException("User is not found");
        }

        if (userService.inMemoryUserStorage.getUsers().get(friendId) == null) {
            throw new NotFoundException("Friend is not found");
        }

        userService.deleteFriend(id, friendId);
    }

    @GetMapping(value = "/{id}/friends")
    public List<User> getFriends(
            @PathVariable Long id
    ) {
        if (userService.inMemoryUserStorage.getUsers().get(id) == null) {
            throw new NotFoundException("User is not found");
        }
        return userService.getFriends(id);
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getMutualFriends(
            @PathVariable Long id,
            @PathVariable Long otherId
    ) {
        return userService.getMutualFriends(id, otherId);
    }
}
