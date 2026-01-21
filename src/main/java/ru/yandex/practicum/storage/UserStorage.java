package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {
    // users CRUDs
    public Collection<User> findAll();

    public User findById(Long userId);

    public User create(@Valid @RequestBody User user);

    public User update(@Valid @RequestBody User user);

    public void delete(@Valid @RequestBody User user);

    // CRUDs of friendship
    public List<User> getFriends(Long id);

    public List<User> getMutualFriends(Long id, Long otherId);

    public Set<Long> getFriendsIdes(Long id);

    public void addFriend(Long id, Long friendId);

    public void deleteFriend(Long id, Long friendId);
}
