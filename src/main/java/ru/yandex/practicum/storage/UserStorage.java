package ru.yandex.practicum.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.model.User;

public interface UserStorage {

    public User create(@Valid @RequestBody User user);

    public User update(@Valid @RequestBody User user);

    public void delete(@Valid @RequestBody User user);
}
