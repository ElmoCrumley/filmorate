package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(@Qualifier("userService") UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        // calling
        log.info("* Calling *, class InMemoryUserStorage, method findAll()");
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        // validation
        log.debug("* Validation * is starting, method create()");
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("* Changing *, Users' name to login");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("* Validation * is passed, method create()");

        // calling
        log.info("* Calling *, class InMemoryUserStorage, method create()");
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        // validation
        log.debug("* Validation * is starting, method update()");
        if (user.getId() == null) {
            throw new NullPointerException("Id должен быть указан");
        }
        log.debug("* Validation * is passed, method update()");

        // calling
        log.info("* Calling *, class InMemoryUserStorage, method update()");
        return userService.update(user);
    }

    @DeleteMapping
    public void delete(@Valid @RequestBody User user) {
        // calling
        log.info("* Calling *, class InMemoryUserStorage, method delete()");
        userService.delete(user);
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) {
        // validation
        log.debug("* Validation * is starting, method addFriend()");
        if (userService.findById(id) == null) {
            throw new NotFoundException("User is not found");
        }

        if (userService.findById(friendId) == null) {
            throw new NotFoundException("Friend is not found");
        }
        log.debug("* Validation * is passed, method addFriend()");

        log.info("Adding the friend {} for user {}", friendId, id);
        //calling
        log.info("* Calling *, class UserService, method addFriend()");
        userService.addFriend(id, friendId);
        log.info("Users' {} friends: {}", id, userService.getFriends(id));
        log.info("Users' {} friends: {}", friendId, userService.getFriends(friendId));
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) {
        // validation
        log.debug("* Validation * is starting, method deleteFriend()");
        if (userService.findById(id) == null) {
            throw new NotFoundException("User is not found");
        }

        if (userService.getFriendsIdes(id).contains(friendId)) {
            throw new NotFoundException("Friend is not found");
        }
        log.debug("* Validation * is passed, method deleteFriend()");

        // calling
        log.info("* Calling *, class UserService, method deleteFriend()");
        userService.deleteFriend(id, friendId);
    }

    @GetMapping(value = "/{id}/friends")
    public List<User> getFriends(
            @PathVariable Long id
    ) {
        //validation
        log.debug("* Validation * is starting, method getFriends()");
        if (userService.findById(id) == null) {
            throw new NotFoundException("User is not found");
        }
        log.debug("* Validation * is passed, method getFriends()");

        // calling
        log.info("* Calling *, class UserService, method getFriends()");
        return userService.getFriends(id);
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getMutualFriends(
            @PathVariable Long id,
            @PathVariable Long otherId
    ) {
        //calling
        log.info("* Calling *, class UserService, method getMutualFriends()");
        return userService.getMutualFriends(id, otherId);
    }
}
