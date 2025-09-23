package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.InMemoryUserStorage;

import java.util.List;
import java.util.Set;

@RestController
@Service
public class UserService {

        public InMemoryUserStorage inMemoryUserStorage;

        @Autowired
        public UserService(InMemoryUserStorage inMemoryUserStorage) {
            this.inMemoryUserStorage = inMemoryUserStorage;
        }

    public void addFriend(Long id, Long friendId) {
        inMemoryUserStorage.getUsers().get(id).getFriends().add(friendId);
        inMemoryUserStorage.getUsers().get(friendId).getFriends().add(id);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = inMemoryUserStorage.getUsers().get(id);

        user.getFriends().remove(friendId);
        inMemoryUserStorage.getUsers().get(friendId).getFriends().remove(id);
    }

    public List<User> getFriends(Long id) {
        Set<Long> friends = inMemoryUserStorage.getUsers().get(id).getFriends();

        return inMemoryUserStorage.getUsers().values().stream()
                .filter(user -> friends.contains(user.getId()))
                .toList();
    }

    public List<User> getMutualFriends(Long id, Long otherId) {
        Set<Long> friends = inMemoryUserStorage.getUsers().get(id).getFriends();
        Set<Long> otherFriends = inMemoryUserStorage.getUsers().get(otherId).getFriends();

        return inMemoryUserStorage.getUsers().values().stream()
                .filter(user -> friends.contains(user.getId()))
                .filter(user -> otherFriends.contains(user.getId()))
                .toList();
    }
}
