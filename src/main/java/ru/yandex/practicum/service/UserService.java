package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // reads
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long userId) {
        return userStorage.findById(userId);
    }

    // other CRUDs
    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void delete(User user) {
        userStorage.delete(user);
    }

    // CRUDs of friendship
    public void addFriend(Long id, Long friendId) {
        userStorage.getFriendsRequests(id).add(friendId);
        userStorage.getFriendsRequests(friendId).add(id);
    }

    public List<User> getFriends(Long id) {
        return userStorage.getFriends(id);
    }

    public Set<Long> getFriendsIdes(Long id) {
        return userStorage.getFriendsConfirmations(id);
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.getFriendsConfirmations(id).remove(id);
        userStorage.getFriendsConfirmations(friendId).remove(friendId);
    }

    public List<User> getMutualFriends(Long id, Long otherId) {
        return userStorage.getMutualFriends(id, otherId);
    }
}
