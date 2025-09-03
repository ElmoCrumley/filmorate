package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.User;

import java.util.Collection;

public interface UserStorage {

    public User create(@Valid @RequestBody User user);

    public User update(@Valid @RequestBody User user);

    public User delete(@Valid @RequestBody User user);
}
