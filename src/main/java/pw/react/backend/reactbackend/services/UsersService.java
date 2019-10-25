package pw.react.backend.reactbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.react.backend.reactbackend.models.User;
import pw.react.backend.reactbackend.repositories.UsersRepository;

import java.util.List;

@Service
public class UsersService {
    private UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public List<User> findAll() {
        return usersRepository.findAll();
    }

    public List<User> findByLogin(String login) {
        return usersRepository.findByLogin(login);
    }

    public User findById(int id) {
        return usersRepository.findById(id);
    }

    public User save(User user) {
        return usersRepository.save(user);
    }

    public void delete(User userToDelete) {
        usersRepository.delete(userToDelete);
    }

    public boolean exists(User user) {
        List<User> result = usersRepository.findByLogin(user.getLogin());
        return result != null && !result.isEmpty();
    }
}
