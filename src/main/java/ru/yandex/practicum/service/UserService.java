package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.InMemoryUserStorage;

import java.util.List;

@RestController
@Service
public class UserService {

    InMemoryUserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable ("id") Long id,
            @PathVariable ("friendId") Long friendId
    ) {
        User user = userStorage.getUsers().get(id);

        user.getFriends().add(friendId);
        userStorage.getUsers().get(friendId).getFriends().add(id);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable ("id") Long id,
            @PathVariable ("friendId") Long friendId
    ) {
        User user = userStorage.getUsers().get(id);

        user.getFriends().remove(friendId);
        userStorage.getUsers().get(friendId).getFriends().remove(id);
    }

    @GetMapping("/users/{id}/friends")
    public List<Long> getFriends(
            @PathVariable ("id") Long id
    ) {
        return userStorage.getUsers().get(id).getFriends().stream().toList();
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<Long> getMutualFriends(
            @PathVariable ("id") Long id,
            @PathVariable ("otherId") Long otherId
    ) {
        return userStorage.getUsers().get(id).getFriends()
                .stream()
                .filter(user -> userStorage.getUsers().get(otherId).getFriends().contains(user))
                .toList();
    }
}
