package ru.yandex.practicum.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// DAO
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    // tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_FRIENDSHIP_CONFIRMED = "friendshipConfirmed";
    // queries
    private static final String FIND_ALL_QUERY = "SELECT * FROM " + TABLE_USERS;
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM " + TABLE_USERS + " WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?) returning id";
    private static final String UPDATE_QUERY = "UPDATE " + TABLE_USERS +
            " set email = ?, login = ?, name = ?, birthday = ? where id = ?";
    private static final String DELETE_QUERY = "delete from " + TABLE_USERS + " where id = ?";
    private static final String GET_FRIENDS_QUERY = "SELECT * FROM " + TABLE_USERS + "AS u"
            + " INNER JOIN " + TABLE_FRIENDSHIP_CONFIRMED + " AS fc ON u.id = fc.confirmed_friend_id"
            + " WHERE fc.user_id = ?";
    private static final String GET_MUTUAL_FRIENDS_QUERY = "SELECT * FROM " + TABLE_USERS
            + " JOIN " + TABLE_FRIENDSHIP_CONFIRMED + " f1 ON u.id = f1.confirmed_friend_id" +
            " JOIN " + TABLE_FRIENDSHIP_CONFIRMED + " f2 ON u.id = f2.confirmed_friend_id"
            + " WHERE f1.user_id = ?" +
            " AND f2.user_id = ?";
    private static final String GET_FRIENDS_IDES = "SELECT confirmed_friend_id FROM " + TABLE_FRIENDSHIP_CONFIRMED
            + " WHERE user_id = ?";
    public static final String INSERT_FRIEND_QUERY = "INSERT INTO friendshipConfirmed(user_id, confirmed_friend_id) " +
            "VALUES (?, ?) returning id";
    public static final String DELETE_FRIEND_QUERY = "delete from " + TABLE_FRIENDSHIP_CONFIRMED
            + " where user_id = ? AND where confirmed_friend_id = ?";

    @Autowired
    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper userRowMapper) {
        this.jdbc = jdbc;
        this.userRowMapper = userRowMapper;
    }

    // users CRUDs
    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_QUERY, userRowMapper);
    }

    @Override
    public User findById(Long userId) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, userRowMapper, userId);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setObject(4, user.getBirthday());
            return stmt;
        }, keyHolder);

        Long generatedId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;

        if (generatedId == null) {
            throw new NotFoundException(" UserStorage, create(), id = null");
        }

        return user;
    }

    @Override
    public User update(User user) {
        jdbc.update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public void delete(User user) {
        jdbc.update(DELETE_QUERY, user.getId());
    }

    // CRUDs of friendship
    @Override
    public List<User> getFriends(Long id) {
        return jdbc.query(GET_FRIENDS_QUERY, userRowMapper, id);
    }

    @Override
    public List<User> getMutualFriends(Long id, Long otherId) {
        return jdbc.query(GET_MUTUAL_FRIENDS_QUERY, userRowMapper, id, otherId);
    }

    @Override
    public Set<Long> getFriendsIdes(Long id) {
        return new HashSet<>(jdbc.queryForList(GET_FRIENDS_IDES, Long.class, id));
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        jdbc.update(INSERT_FRIEND_QUERY, id, friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
    }
}
