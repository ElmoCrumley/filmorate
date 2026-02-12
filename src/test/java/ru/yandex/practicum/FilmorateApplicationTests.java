package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MotionPictureAA;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.FilmDbStorage;
import ru.yandex.practicum.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan(basePackages = "ru.yandex.practicum.storage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    static User transferredUser1;
    static User transferredUser2;
    static User transferredUser3;
    private final FilmDbStorage filmDbStorage;
    static Film transferredFilm1;
    static Film transferredFilm2;
    static Film transferredFilm3;

    @BeforeEach
    void setUp() {
        transferredUser1 = new User();
        transferredUser2 = new User();
        transferredUser3 = new User();
        transferredUser1.setEmail("sd@ds.com");
        transferredUser2.setEmail("sdd@ddss.com");
        transferredUser3.setEmail("sdwwwwd@ddss.com");
        transferredUser1.setLogin("sakdjv");
        transferredUser2.setLogin("sakdwedcwscjv");
        transferredUser3.setLogin("sakdwwefvweewvervedcwscjv");
        transferredUser1.setBirthday(LocalDate.of(1970,10,11));
        transferredUser2.setBirthday(LocalDate.of(1965,12,8));
        transferredUser3.setBirthday(LocalDate.of(1975,4,2));
        userStorage.create(transferredUser1);
        userStorage.create(transferredUser2);
        userStorage.create(transferredUser3);
        transferredFilm1 = new Film();
        transferredFilm2 = new Film();
        transferredFilm3 = new Film();
        transferredFilm1.setName("filmname1");
        transferredFilm2.setName("filmname2");
        transferredFilm3.setName("filmname3");
        transferredFilm1.setReleaseDate(LocalDate.ofEpochDay(1989-10-11));
        transferredFilm2.setReleaseDate(LocalDate.ofEpochDay(1991-10-11));
        transferredFilm3.setReleaseDate(LocalDate.ofEpochDay(1999-10-11));
        transferredFilm1.getMpa().setId(1);
        transferredFilm2.getMpa().setId(2);
        transferredFilm3.getMpa().setId(3);
        filmDbStorage.include(transferredFilm1);
        filmDbStorage.include(transferredFilm2);
        filmDbStorage.include(transferredFilm3);
    }

    // UserDbStorage tests
    // users CRUDs
    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testCreateUser() {
        User receivedUser1 = userStorage.findById(transferredUser1.getId()).isPresent()
                ? userStorage.findById(transferredUser1.getId()).get() : null;
        User receivedUser2 = userStorage.findById(transferredUser2.getId()).isPresent()
                ? userStorage.findById(transferredUser2.getId()).get() : null;

        assertThat(receivedUser1).isNotNull();
        assertThat(receivedUser2).isNotNull();
        assertThat(receivedUser1.getId()).isEqualTo(transferredUser1.getId());
        assertThat(receivedUser2.getId()).isEqualTo(transferredUser2.getId());
        assertThat(receivedUser1.getEmail()).isEqualTo(transferredUser1.getEmail());
        assertThat(receivedUser2.getEmail()).isEqualTo(transferredUser2.getEmail());
        assertThat(receivedUser1.getLogin()).isEqualTo(transferredUser1.getLogin());
        assertThat(receivedUser2.getLogin()).isEqualTo(transferredUser2.getLogin());
        assertThat(receivedUser1.getBirthday()).isEqualTo(transferredUser1.getBirthday());
        assertThat(receivedUser2.getBirthday()).isEqualTo(transferredUser2.getBirthday());
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testDeleteUser() {
        userStorage.delete(transferredUser1);
        userStorage.delete(transferredUser2);

        assertThat(userStorage.findById(transferredUser1.getId()).isEmpty()).isTrue();
        assertThat(userStorage.findById(transferredUser2.getId()).isEmpty()).isTrue();
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindUserById() {
        Optional<User> receivedUser1 = userStorage.findById(transferredUser1.getId());
        Optional<User> receivedUser2 = userStorage.findById(transferredUser2.getId());

        assertThat(receivedUser1)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", transferredUser1.getId())
                );
        assertThat(receivedUser2)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", transferredUser2.getId())
                );
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindAllUsers() {
        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(3);
        userStorage.delete(transferredUser2);
        users = userStorage.findAll();
        assertThat(users).hasSize(2);
        userStorage.delete(transferredUser1);
        users = userStorage.findAll();
        assertThat(users).hasSize(1);
        userStorage.delete(transferredUser3);
        assertThrows(NotFoundException.class, userStorage::findAll);
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testUpdateUser() {
        User receivedUser1 = userStorage.findById(transferredUser1.getId()).isPresent()
                ? userStorage.findById(transferredUser1.getId()).get() : null;
        User receivedUpdatedUser1;

        String email = "ff@ff.com";
        String login = "Gh";
        LocalDate birthday = LocalDate.ofEpochDay(1111-11-11);

        if (receivedUser1 != null) {
            receivedUser1.setLogin(login);
            receivedUser1.setEmail(email);
            receivedUser1.setBirthday(birthday);
            userStorage.update(receivedUser1);
            receivedUpdatedUser1 = userStorage.findById(receivedUser1.getId()).isPresent()
                    ? userStorage.findById(receivedUser1.getId()).get() : null;
            assertThat(receivedUpdatedUser1).isNotNull();
            assertThat(receivedUpdatedUser1.getId()).isEqualTo(receivedUser1.getId());
            assertThat(receivedUpdatedUser1.getEmail()).isEqualTo(email);
            assertThat(receivedUpdatedUser1.getLogin()).isEqualTo(login);
            assertThat(receivedUpdatedUser1.getBirthday()).isEqualTo(birthday);
        } else {
            fail("receivedUser1 not found");
        }
    }

    // CRUDs of friendship
    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testAddFriendToUser() {
        log.info("------------- * testAddFriendToUser * тест запустился -------------");
        userStorage.addFriend(transferredUser1.getId(), transferredUser2.getId());

        User receivedUser1 = userStorage.findById(transferredUser1.getId()).isPresent()
                ? userStorage.findById(transferredUser1.getId()).get() : null;

        if (receivedUser1 != null) {
            Set<Long> friends = receivedUser1.getFriendshipConfirmed();
            assertThat(friends).hasSize(1);
        } else {
            fail("receivedUser1 not found");
        }
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testGetFriendsOfUser() {
        userStorage.addFriend(transferredUser1.getId(), transferredUser2.getId());

        List<User> users = userStorage.getFriends(transferredUser1.getId());

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(transferredUser2.getId());
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testDeleteUsersFriend() {
        userStorage.addFriend(transferredUser1.getId(), transferredUser2.getId());
        assertThat(userStorage.getFriends(transferredUser1.getId())).hasSize(1);
        userStorage.deleteFriend(transferredUser1.getId(), transferredUser2.getId());
        assertThat(userStorage.getFriends(transferredUser1.getId())).isEmpty();
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testGetMutualFriendsOfUsers() {
        userStorage.addFriend(transferredUser1.getId(), transferredUser3.getId());
        userStorage.addFriend(transferredUser2.getId(), transferredUser3.getId());

        List<User> users = userStorage.getMutualFriends(transferredUser1.getId(), transferredUser2.getId());

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(transferredUser3.getId());
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testGetFriendsIdesOfUser() {
        userStorage.addFriend(transferredUser1.getId(), transferredUser2.getId());
        userStorage.addFriend(transferredUser1.getId(), transferredUser3.getId());

        assertThat(userStorage.getFriendsIdes(transferredUser1.getId())).hasSize(2);
    }

    // FilmDbStorage tests
    // films CRUDs
    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testCreateFilm() {
        Film receivedFilm1 = filmDbStorage.findById(transferredFilm1.getId()).isPresent()
                ? filmDbStorage.findById(transferredFilm1.getId()).get() : null;
        Film receivedFilm2 = filmDbStorage.findById(transferredFilm2.getId()).isPresent()
                ? filmDbStorage.findById(transferredFilm2.getId()).get() : null;

        assertThat(receivedFilm1).isNotNull();
        assertThat(receivedFilm2).isNotNull();
        assertThat(receivedFilm1.getId()).isEqualTo(transferredFilm1.getId());
        assertThat(receivedFilm2.getId()).isEqualTo(transferredFilm2.getId());
        assertThat(receivedFilm1.getName()).isEqualTo(transferredFilm1.getName());
        assertThat(receivedFilm2.getName()).isEqualTo(transferredFilm2.getName());
        assertThat(receivedFilm1.getReleaseDate()).isEqualTo(transferredFilm1.getReleaseDate());
        assertThat(receivedFilm2.getReleaseDate()).isEqualTo(transferredFilm2.getReleaseDate());
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testDeleteFilm() {
        filmDbStorage.delete(transferredFilm1);
        filmDbStorage.delete(transferredFilm2);

        assertThat(filmDbStorage.findById(transferredFilm1.getId()).isEmpty()).isTrue();
        assertThat(filmDbStorage.findById(transferredFilm2.getId()).isEmpty()).isTrue();
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindByIdFilm() {
        Optional<Film> receivedFilm1 = filmDbStorage.findById(transferredFilm1.getId());
        Optional<Film> receivedFilm2 = filmDbStorage.findById(transferredFilm2.getId());

        assertThat(receivedFilm1)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", transferredFilm1.getId())
                );
        assertThat(receivedFilm2)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", transferredFilm2.getId())
                );
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindAllFilms() {
        Collection<Film> films = filmDbStorage.findAll();

        assertThat(films).hasSize(3);
        filmDbStorage.delete(transferredFilm2);
        films = filmDbStorage.findAll();
        assertThat(films).hasSize(2);
        filmDbStorage.delete(transferredFilm1);
        films = filmDbStorage.findAll();
        assertThat(films).hasSize(1);
        filmDbStorage.delete(transferredFilm3);
        assertThrows(NotFoundException.class, filmDbStorage::findAll);
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindFilmById() {
        Optional<User> userOptional = userStorage.findById(transferredUser1.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", userOptional.get().getId())
                );
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testUpdateFilm() {
        Film receivedFilm1 = filmDbStorage.findById(transferredFilm1.getId()).isPresent()
                ? filmDbStorage.findById(transferredFilm1.getId()).get() : null;
        Film receivedUpdatedFilm1;

        String name = "Cool film";
        LocalDate releaseDate = LocalDate.ofEpochDay(1222-2-2);

        if (receivedFilm1 != null) {
            receivedFilm1.setName(name);
            receivedFilm1.setReleaseDate(releaseDate);
            filmDbStorage.update(receivedFilm1);
            receivedUpdatedFilm1 = filmDbStorage.findById(receivedFilm1.getId()).isPresent()
                    ? filmDbStorage.findById(receivedFilm1.getId()).get() : null;
            assertThat(receivedUpdatedFilm1).isNotNull();
            assertThat(receivedUpdatedFilm1.getId()).isEqualTo(receivedFilm1.getId());
            assertThat(receivedUpdatedFilm1.getName()).isEqualTo(name);
            assertThat(receivedUpdatedFilm1.getReleaseDate()).isEqualTo(releaseDate);
        } else {
            fail("receivedFilm1 not found");
        }
    }

    // CRUDs of likes
    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testAddLike() {
        long filmId1 = transferredFilm1.getId();
        long filmId2 = transferredFilm2.getId();
        long filmId3 = transferredFilm3.getId();

        filmDbStorage.addLike(filmId1, filmId2);
        filmDbStorage.addLike(filmId1, filmId3);

        Film receivedFilm1 = filmDbStorage.findById(transferredFilm1.getId()).isPresent()
                ? filmDbStorage.findById(transferredFilm1.getId()).get() : null;

        if (receivedFilm1 != null) {
            assertThat(receivedFilm1.getLikes()).isNotEmpty();
            assertThat(receivedFilm1.getLikes()).hasSize(2);
        } else {
            fail("receivedFilm1 not found");
        }
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testDeleteLike() {
        long filmId1 = transferredFilm1.getId();
        long filmId2 = transferredFilm2.getId();
        long filmId3 = transferredFilm3.getId();

        filmDbStorage.addLike(filmId1, filmId2);
        filmDbStorage.addLike(filmId1, filmId3);
        filmDbStorage.deleteLike(filmId1, filmId3);

        Film receivedFilm1 = filmDbStorage.findById(transferredFilm1.getId()).isPresent()
                ? filmDbStorage.findById(transferredFilm1.getId()).get() : null;

        if (receivedFilm1 != null) {
            assertThat(receivedFilm1.getLikes()).isNotEmpty();
            assertThat(receivedFilm1.getLikes()).hasSize(1);
        } else {
            fail("receivedFilm1 not found");
        }
    }

    // read populars
    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testGetMostPopular() {
        long filmId1 = transferredFilm1.getId();
        long filmId2 = transferredFilm2.getId();
        long filmId3 = transferredFilm3.getId();

        filmDbStorage.addLike(filmId2, filmId1);
        filmDbStorage.addLike(filmId2, filmId3);
        filmDbStorage.addLike(filmId3, filmId1);
        List<Film> mostPopularFilms1 = filmDbStorage.getMostPopular(2);
        List<Film> mostPopularFilms2 = filmDbStorage.getMostPopular(1);

        assertThat(mostPopularFilms1).hasSize(2);
        assertThat(mostPopularFilms2).hasSize(1);
    }

    // genres
    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindAllGenres() {
        Collection<Genre> genres = filmDbStorage.findAllGenres();

        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSize(6);
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindGenreById() {
        Genre genre = filmDbStorage.findGenreById(1).isPresent()
                ? filmDbStorage.findGenreById(1).get() : null;

        if (genre != null) {
            assertThat(genre.getName()).isEqualTo("Комедия");
        }
    }

    // mpa's
    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindAllMPAs() {
        Collection<MotionPictureAA> mpas = filmDbStorage.findAllMPAs();

        assertThat(mpas).isNotEmpty();
        assertThat(mpas).hasSize(5);
    }

    @Test
    @Sql(scripts = "classpath:test-data.sql")
    public void testFindMPAById() {
        MotionPictureAA mpa = filmDbStorage.findMPAById(1).isPresent()
                ? filmDbStorage.findMPAById(1).get() : null;

        if (mpa != null) {
            assertThat(mpa.getName()).isEqualTo("G");
        }
    }
}
