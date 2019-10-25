package pw.react.backend.reactbackend;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pw.react.backend.reactbackend.controllers.UsersController;
import pw.react.backend.reactbackend.errors.ErrorResponse;
import pw.react.backend.reactbackend.errors.UserAlreadyExistsException;
import pw.react.backend.reactbackend.errors.UserNotFoundException;
import pw.react.backend.reactbackend.models.User;
import pw.react.backend.reactbackend.repositories.UsersRepository;
import pw.react.backend.reactbackend.services.UsersService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("dev")
@RunWith(MockitoJUnitRunner.class)
public class ReactBackendApplicationTests {
    private UsersController usersController;

    @Spy
    @InjectMocks
    private UsersService usersService;

    @Mock
    private UsersRepository usersRepository;

    private static User[] users = {
            new User().setAllDetails("wiczolekp", "przemek", "wiczolek", true),
            new User().setAllDetails("kaladin", "kaladin", "",
                    LocalDate.of(100, 10, 1), true),
            new User().setAllDetails("davars", "shallan", "davar", true),
            new User().setAllDetails("eodin", "eodin", "", false),
            new User().setAllDetails("kholind", "dalinar", "kholin",
                    LocalDate.of(80, 3, 5), true),
    };

    @Before
    public void setUp() {
        usersController = new UsersController(usersService);

    }

    @Test
    public void givenUsersFromRepository_whenGetUsersIsInvoked_thenReturnAllUsers() {
        given(usersRepository.findAll()).willReturn(Arrays.asList(users));

        // when
        ResponseEntity<List<User>> response = usersController.getUsers(null);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).hasSize(users.length);
        then(response.getBody()).containsExactly(users);
    }

    @Test
    public void givenLogin_whenGetUsersWithLoginIsInvoked_thenReturnUserWithProvidedLoign() {
        // given
        String login = users[users.length - 1].getLogin();
        List<User> responseUsers = Arrays.stream(users).filter(user -> user.getLogin().equals(login)).collect(Collectors.toList());
        given(usersRepository.findByLogin(login)).willReturn(responseUsers);

        // when
        ResponseEntity<List<User>> response = usersController.getUsers(login);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).hasSize(1);
        then(response.getBody()).containsExactly(responseUsers.get(0));
    }

    @Test(expected = UserNotFoundException.class)
    public void givenInvalidLogin_whenGetUsersWithLoginIsInvoked_thenThrowException() {
        // given
        String login = "abba";
        given(usersRepository.findByLogin(login)).willReturn(null);

        when(usersController.getUsers(login)).
                thenThrow(UserNotFoundException.class);
    }

    @Test
    public void givenUserId_whenGetUserByIdIsInvoked_thenReturnUserWithGivenId() {
        // given
        int id = users[users.length - 2].getId();
        User responseUser = Arrays.stream(users).filter(user -> user.getId() == id).findFirst().get();
        given(usersRepository.findById(id)).willReturn(responseUser);

        // when
        ResponseEntity<User> response = usersController.getUser(id);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualToComparingFieldByField(responseUser);
    }

    @Test(expected = UserNotFoundException.class)
    public void givenInvalidUserId_whenGetUserByIdIsInvoked_thenThrowException() {
        // given
        int id = -1;
        given(usersRepository.findById(id)).willReturn(null);

        when(usersController.getUser(id)).
                thenThrow(UserNotFoundException.class);
    }


    @Test
    public void givenNewUser_whenCreateUserIsInvoked_thenReturnSavedUser() {
        // given
        User user = new User().setAllDetails("login", "a", "b", false);
        given(usersRepository.findByLogin("login")).willReturn(null);
        given(usersRepository.save(user)).willReturn(user);

        // when
        ResponseEntity<User> response = usersController.createUser(user);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualToComparingFieldByField(user);
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void givenNewUserWithExistingLogin_whenCreateUserIsInvoked_thenThrowException() {
        // given
        User user = new User().setAllDetails(users[2].getLogin(), "a", "b", false);
        given(usersRepository.findByLogin(users[2].getLogin())).willReturn(Collections.singletonList(users[2]));

        when(usersController.createUser(user)).
                thenThrow(UserAlreadyExistsException.class);
    }

    @Test
    public void givenUpdatedUser_whenUpdateUserIsInvoked_thenReturnUpdatedUser() {
        // given
        User updatedUser = new User().setAllDetails("login", users[0].getFirstName(), "b",
                users[0].getDateOfBirth(), false);
        int id = users[0].getId();
        updatedUser.setId(id);
        given(usersRepository.findById(id)).willReturn(users[0]);
        given(usersRepository.save(users[0])).willReturn(users[0]);

        // when
        ResponseEntity<User> response = usersController.updateUser(id, updatedUser);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isEqualToComparingFieldByField(updatedUser);
    }

    @Test
    public void givenUserId_whenDeleteUserIsInvoked_thenReturnValidResponse() {
        // given
        int id = users[1].getId();
        given(usersRepository.findById(id)).willReturn(users[1]);

        // when
        ResponseEntity<Map<String, Boolean>> response = usersController.deleteUser(id);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).containsExactly(new AbstractMap.SimpleEntry<>("deleted", Boolean.TRUE));
    }

    @Test
    public void givenUserNotFoundException_whenNotFoundIsInvoked_thenReturnErrorResponse() {
        // given
        UserNotFoundException ex = new UserNotFoundException();

        // when
        ResponseEntity<ErrorResponse> response = usersController.notFound(ex);

        then(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        then(body.getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void givenUserAlreadyExistsException_whenAlreadyExistsIsInvoked_thenReturnErrorResponse() {
        // given
        UserAlreadyExistsException ex = new UserAlreadyExistsException("msg");

        // when
        ResponseEntity<ErrorResponse> response = usersController.alreadyExists(ex);

        then(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        then(body.getMessage()).isEqualTo("msg");
        then(body.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
