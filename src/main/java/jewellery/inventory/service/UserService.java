package jewellery.inventory.service;

import static jewellery.inventory.constant.ApplicationConstants.DUPLICATE_EMAIL_ERROR_MSG;
import static jewellery.inventory.constant.ApplicationConstants.DUPLICATE_NAME_ERROR_MSG;
import static jewellery.inventory.constant.ApplicationConstants.USER_NOT_FOUND_ERROR_MSG;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.exception.DuplicateEmailException;
import jewellery.inventory.exception.DuplicateNameException;
import jewellery.inventory.exception.UserNotFoundException;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
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
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR_MSG + id));
  }

  public User createUser(User user) {
    if (checkIfEmailExists(user.getEmail())) {
      throw new DuplicateEmailException(DUPLICATE_EMAIL_ERROR_MSG);
    }

    if (checkIfNameExists(user.getName())) {
      throw new DuplicateNameException(DUPLICATE_NAME_ERROR_MSG);
    }
    return userRepository.save(user);
  }

  public User updateUser(User user) {
    if (userRepository.findById(user.getId()).isEmpty()) {
      throw new UserNotFoundException(USER_NOT_FOUND_ERROR_MSG + user.getId());
    }

    if (checkIfNameExists(user.getName(), user.getId())) {
      throw new DuplicateNameException(DUPLICATE_NAME_ERROR_MSG);
    }

    if (checkIfEmailExists(user.getEmail(), user.getId())) {
      throw new DuplicateEmailException(DUPLICATE_EMAIL_ERROR_MSG);
    }

    return userRepository.save(user);
  }

  public void deleteUser(UUID id) {
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
    } else {
      throw new UserNotFoundException(USER_NOT_FOUND_ERROR_MSG + id);
    }
  }

  private boolean checkIfEmailExists(String email) {
    Optional<User> user = userRepository.findByEmail(email);
    return user.isPresent();
  }

  private boolean checkIfEmailExists(String email, UUID id) {
    Optional<User> existingUser = userRepository.findByEmail(email);
    return existingUser.isPresent() && !existingUser.get().getId().equals(id);
  }

  private boolean checkIfNameExists(String name) {
    Optional<User> user = userRepository.findByName(name);
    return user.isPresent();
  }

  private boolean checkIfNameExists(String name, UUID id) {
    Optional<User> existingUser = userRepository.findByName(name);
    return existingUser.isPresent() && !existingUser.get().getId().equals(id);
  }
}
