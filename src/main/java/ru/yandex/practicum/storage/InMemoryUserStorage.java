package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        log.trace("method * findAll(), Getting a list of users");
        return users.values();
    }

    @Override
    public User create(@Valid @RequestBody User user) {
        user.setId(getNextId());
        log.trace("method * create(), Set an ID");
        users.put(user.getId(), user);
        log.trace("method * create(), Put the user");
        return user;
    }

    @Override
    public User update(@Valid @RequestBody User user) {
        Long userId = user.getId();
        log.trace("method * update(), Field userId has been created");

        if (users.containsKey(userId)) {
            User oldUser = users.get(userId);
            log.trace("method * update(), Field oldUser has been created");
            oldUser.setBirthday(user.getBirthday());
            log.trace("method * update(), Set the birthday");
            oldUser.setLogin(user.getLogin());
            log.trace("method * update(), Set the login");
            oldUser.setName(user.getName());
            log.trace("method * update(), Set the name");
            oldUser.setEmail(user.getEmail());
            log.trace("method * update(), Set the email");
            return oldUser;
        }

        log.error("method * update(), User with this ID is not found");
        throw new NotFoundException("Пользователь с ID = " + userId + " не найден");
    }

    public void delete(@Valid @RequestBody User user) {
        users.remove(user.getId());
    }

    private long getNextId() {
        log.trace("method * getNextId(), Creating an ID");
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
