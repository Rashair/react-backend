package pw.react.backend.reactbackend.errors;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        this("");
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
