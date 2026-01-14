package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {

    public Collection<User> findAll();

    public User findById(Long userId);

    public User create(@Valid @RequestBody User user);

    public User update(@Valid @RequestBody User user);

    public void delete(@Valid @RequestBody User user);

    public List<User> getFriends(Long id);

    public Set<Long> getFriendsRequests(Long id);

    public Set<Long> getFriendsConfirmations(Long id);

    public List<User> getMutualFriends(Long id, Long otherId);
}
