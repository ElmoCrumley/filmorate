package ru.yandex.practicum.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

// DAO
@Slf4j
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    // tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_FRIENDSHIP_CONFIRMED = "friendshipConfirmed";
    // queries
    private static final String FIND_ALL_QUERY = "SELECT u.id, u.email, u.login, u.name, u.birthday, " +
            "fc.confirmed_friend_id " +
            "FROM users u " +
            "LEFT JOIN friendshipConfirmed fc ON u.id = fc.user_id ";
    private static final String FIND_BY_ID_QUERY = "SELECT u.id, u.email, u.login, u.name, u.birthday, " +
            "fc.confirmed_friend_id " +
            "FROM users u " +
            "LEFT JOIN friendshipConfirmed fc ON u.id = fc.user_id " +
            "WHERE u.id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE " + TABLE_USERS +
            " set email = ?, login = ?, name = ?, birthday = ? where id = ?";
    private static final String DELETE_QUERY = "delete from " + TABLE_USERS + " where id = ?";
    private static final String GET_FRIENDS_QUERY = "SELECT * FROM " + TABLE_USERS + " AS u " +
            "INNER JOIN " + TABLE_FRIENDSHIP_CONFIRMED + " AS fc ON u.id = fc.confirmed_friend_id " +
            "WHERE fc.user_id = ?";
    private static final String GET_MUTUAL_FRIENDS_QUERY = "SELECT u.id, u.email, u.login, u.name, u.birthday, " +
            "fc.confirmed_friend_id " +
            "FROM users u " +
            "LEFT JOIN friendshipConfirmed fc ON u.id = fc.user_id " +
            "JOIN friendshipConfirmed f1 ON u.id = f1.confirmed_friend_id " +
            "JOIN friendshipConfirmed f2 ON u.id = f2.confirmed_friend_id " +
            "WHERE f1.user_id = ? " +
            "AND f2.user_id = ?";
    private static final String GET_FRIENDS_IDES = "SELECT confirmed_friend_id FROM " + TABLE_FRIENDSHIP_CONFIRMED +
            " WHERE user_id = ?";
    public static final String INSERT_FRIEND_QUERY = "INSERT INTO friendshipConfirmed(user_id, confirmed_friend_id) " +
            "VALUES (?, ?)";
    public static final String DELETE_FRIEND_QUERY = "delete from " + TABLE_FRIENDSHIP_CONFIRMED +
            " where user_id = ? AND confirmed_friend_id = ?";

    @Autowired
    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper userRowMapper) {
        this.jdbc = jdbc;
        this.userRowMapper = userRowMapper;
    }

    // users CRUDs
    @Override
    public Collection<User> findAll() {
        log.info("------------- * Start / Finish * UserDbStorage * findAll() -------------");

        UserRowMapper userRowMapper = new UserRowMapper();

        List<User> users = jdbc.query(FIND_ALL_QUERY, userRowMapper);

        if (users.isEmpty()) {
            throw new NotFoundException("There are no data records in the table");
        }

        for (User user : users) {
            log.info("* UserDbStorage * findAll() {} ", user);
        }

        return users;
    }

    @Override
    public Optional<User> findById(Long userId) {
        log.info("------------- * Start / Finish * UserDbStorage * findById() -------------");
        try {
            List<User> users = jdbc.query(FIND_BY_ID_QUERY, userRowMapper, userId);

            if (users.isEmpty()) {
                return  Optional.empty();
            } else {
                return Optional.of(users.get(0));
            }
        } catch (DataAccessException e) {
            throw new NotFoundException("user is not found");
        }
    }

    @Override
    public User create(User user) {
        log.info("\n------------- * Start * UserDbStorage * create() -------------");
        KeyHolder keyHolder = new GeneratedKeyHolder();

        log.info("Email: {} * Login: {} * Name: {} * Birthday: {}",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        log.info("* Calling * UserDbStorage * create().update()-");
        log.info("* SQL * UserDbStorage * create().update()- \n    " + INSERT_QUERY);

        try {
            jdbc.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getLogin());
                stmt.setString(3, user.getName());
                LocalDateTime birthdayTime = user.getBirthday().atStartOfDay();
                stmt.setObject(4, birthdayTime);
                return stmt;
            }, keyHolder);
        } catch (DataAccessException e) {
            log.error("* Exception * SQLException occurred while executing insert: {}", e.getMessage());
        }

        log.info("* Implementing * UserDbStorage * create()");

        Long generatedId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;

        user.setId(generatedId);

        if (generatedId == null) {
            throw new NotFoundException(" UserDbStorage, create(), id = null");
        }

        log.info("------------- * Finish * UserDbStorage * create() -------------\n");
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        log.info("------------- * Start * UserDbStorage * update() -------------");

        int rowsAffected = jdbc.update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Id is not found");
        }

        log.info("------------- * Finish * UserDbStorage * update() -------------");
        return Optional.of(user);
    }

    @Override
    public void delete(User user) {
        log.info("------------- * Start / Finish * UserDbStorage * delete() -------------");
        jdbc.update(DELETE_QUERY, user.getId());
    }

    // CRUDs of friendship
    @Override
    public List<User> getFriends(Long id) {
        log.info("------------- * Start / Finish * UserDbStorage * getFriends() -------------");
        return jdbc.query(GET_FRIENDS_QUERY, userRowMapper, id);
    }

    @Override
    public List<User> getMutualFriends(Long id, Long otherId) {
        log.info("------------- * Start / Finish * UserDbStorage * getMutualFriends() -------------");
        return jdbc.query(GET_MUTUAL_FRIENDS_QUERY, userRowMapper, id, otherId);
    }

    @Override
    public Set<Long> getFriendsIdes(Long id) {
        log.info("------------- * Start / Finish * UserDbStorage * getFriendsIdes() -------------");
        HashSet<Long> friendsIdes = new HashSet<>();

        for (Long friendId : jdbc.queryForList(GET_FRIENDS_IDES, Long.class, id)) {
            if (friendId != null) {
                friendsIdes.add(friendId);
            }
        }

        return friendsIdes;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        log.info("------------- * Start / Finish * UserDbStorage * addFriend() -------------");
        if (findById(friendId).isEmpty()) {
            throw new NotFoundException("friend is not found");
        } else {
            jdbc.update(INSERT_FRIEND_QUERY, id, friendId);
        }
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        log.info("------------- * Start / Finish * UserDbStorage * deleteFriend() -------------");
        if (findById(friendId).isEmpty()) {
            throw new NotFoundException("friend is not found");
        } else {
            jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
        }
    }
}
