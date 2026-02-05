package ru.yandex.practicum.storage.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.User;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserRowMapper implements RowMapper<User> {
    private final Map<Long, User> userMap = new HashMap<>();

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        long userId = rs.getLong("id");

        User user = userMap.get(userId);

        if (user == null) {
            user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
        }

        Long friendId = rs.getObject("confirmed_friend_id", Long.class);

        if (friendId != null) {
            user.getFriendshipConfirmed().add(friendId);
        }

        return user;
    }

    public Collection<User> getUsers() {
        return userMap.values();
    }
}
