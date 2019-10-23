package pw.react.backend.reactbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pw.react.backend.reactbackend.errors.ErrorResponse;
import pw.react.backend.reactbackend.errors.UserAlreadyExistsException;
import pw.react.backend.reactbackend.errors.UserNotFoundException;
import pw.react.backend.reactbackend.models.User;
import pw.react.backend.reactbackend.services.UsersService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spring-demo")
public class UsersController {
    private UsersService usersService;

    @Autowired
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers(@RequestParam(required = false) String login) {
        List<User> result;
        if (login != null && login.length() > 0)
            result = usersService.findByLogin(login);
        else
            result = usersService.findAll();
        if (result == null) {
            throw new UserNotFoundException("Login: " + login);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable(value = "id") int id) {
        User result = usersService.findById(id);
        if (result == null) {
            throw new UserNotFoundException("Id: " + id);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        if (usersService.exists(user)) {
            throw new UserAlreadyExistsException("Login: " + user.getLogin());
        }

        User result = usersService.save(user);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable(value = "id") int id, @Valid @RequestBody User user) {
        User userToUpdate = usersService.findById(id);
        if (userToUpdate == null) {
            throw new UserNotFoundException("Id: " + id);
        }

        userToUpdate.setAllDetails(user.getLogin(), user.getFirstName(), user.getLastName(), user.getDateOfBirth(),
                user.getIsActive());
        final User updatedUser = usersService.save(userToUpdate);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable(value = "id") int id) {
        User userToDelete = usersService.findById(id);
        if (userToDelete == null) {
            throw new UserNotFoundException("Id: " + id);
        }

        usersService.delete(userToDelete);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler({UserAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> alreadyExists(UserAlreadyExistsException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> notFound(UserNotFoundException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(), "The user was not found"),
                HttpStatus.NOT_FOUND);
    }
}
