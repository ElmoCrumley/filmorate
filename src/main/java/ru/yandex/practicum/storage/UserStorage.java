package ru.yandex.practicum.storage;

import ru.yandex.practicum.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    // users CRUDs
    public Collection<User> findAll();

    public Optional<User> findById(Long userId);

    public User create(User user);

    public Optional<User> update(User user);

    public void delete(User user);

    // CRUDs of friendship
    public List<User> getFriends(Long id);

    public List<User> getMutualFriends(Long id, Long otherId);

    public Set<Long> getFriendsIdes(Long id);

    public void addFriend(Long id, Long friendId);

    public void deleteFriend(Long id, Long friendId);
}
