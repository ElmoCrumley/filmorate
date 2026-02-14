package ru.yandex.practicum.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.User;

import java.util.*;

@Component
@Getter
@Slf4j
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    // users CRUDs
    @Override
    public Collection<User> findAll() {
        log.trace("method * findAll(), Getting a list of users");
        return List.copyOf(users.values());
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.of(users.get(userId));
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        log.trace("method * create(), Set an ID");
        users.put(user.getId(), user);
        log.trace("method * create(), Put the user");
        return user;
    }

    @Override
    public Optional<User> update(User user) {
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
            return Optional.of(oldUser);
        }

        log.error("method * update(), User with this ID is not found");
        throw new NotFoundException("Пользователь с ID = " + userId + " не найден");
    }

    public void delete(User user) {
        users.remove(user.getId());
    }

    // CRUDs of friendship
    @Override
    public List<User> getFriends(Long id) {
        return  findAll().stream()
                .filter(user -> getFriendsIdes(id).contains(user.getId()))
                .toList();
    }

    public Set<Long> getFriendsRequests(Long id) {
        return users.get(id).getFriendshipRequests();
    }

    @Override
    public Set<Long> getFriendsIdes(Long id) {
        return users.get(id).getFriendshipConfirmed();
    }

    @Override
    public List<User> getMutualFriends(Long id, Long otherId) {
        Set<Long> friends = getFriendsIdes(id);
        Set<Long> otherFriends = getFriendsIdes(otherId);

        return findAll().stream()
                .filter(user -> friends.contains(user.getId()))
                .filter(user -> otherFriends.contains(user.getId()))
                .toList();
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        getFriendsRequests(id).add(friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        getFriendsIdes(id).remove(friendId);
    }

    // create next ID
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
