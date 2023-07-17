package jewellery.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.exceptions.DuplicateEmailException;
import jewellery.inventory.exceptions.DuplicateNameException;
import jewellery.inventory.exceptions.UserNotFoundException;
import jewellery.inventory.model.User;
import jewellery.inventory.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID id) {
        return userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public User createUser(User user) {
        if (checkIfEmailExists(user.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        if (checkIfNameExists(user.getName())) {
            throw new DuplicateNameException("Name already exists");
        }

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        if (userRepository.findById(user.getId()).isPresent()) {
            return userRepository.save(user);
        } else {
            throw new UserNotFoundException("User not found with id: " + user.getId());
        }
    }

    public void deleteUser(UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }

    private boolean checkIfEmailExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }

    private boolean checkIfNameExists(String name) {
        Optional<User> user = userRepository.findByName(name);
        return user.isPresent();
    }
}
