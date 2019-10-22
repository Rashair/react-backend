package pw.react.backend.reactbackend.errors;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        this("");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
