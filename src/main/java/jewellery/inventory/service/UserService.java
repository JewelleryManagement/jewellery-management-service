package jewellery.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.UserRequest;
import jewellery.inventory.dto.UserResponse;
import jewellery.inventory.exception.DuplicateEmailException;
import jewellery.inventory.exception.DuplicateNameException;
import jewellery.inventory.exception.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<UserResponse> getAllUsers() {
    return UserMapper.INSTANCE.toUserResponseList(userRepository.findAll());
  }

  public UserResponse getUser(UUID id) {
    return UserMapper.INSTANCE.toUserResponse(
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
  }

  public UserResponse createUser(UserRequest user) {
    if (checkIfEmailExists(user.getEmail(), null)) {
      throw new DuplicateEmailException(user.getEmail());
    }

    if (checkIfNameExists(user.getName(), null)) {
      throw new DuplicateNameException(user.getName());
    }
    return UserMapper.INSTANCE.toUserResponse(
        userRepository.save(UserMapper.INSTANCE.toUserEntity(user)));
  }

  public UserResponse updateUser(UserRequest userRequest, UUID id) {
    User existingUser =
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

    User userToUpdate = UserMapper.INSTANCE.toUserEntity(userRequest);
    userToUpdate.setId(existingUser.getId());

    validateUserDetails(userToUpdate);

    return UserMapper.INSTANCE.toUserResponse(userRepository.save(userToUpdate));
  }

  public void deleteUser(UUID id) {
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
    } else {
      throw new UserNotFoundException(id);
    }
  }

  private void validateUserDetails(User user) {
    if (checkIfNameExists(user.getName(), user.getId())) {
      throw new DuplicateNameException(user.getName());
    }

    if (checkIfEmailExists(user.getEmail(), user.getId())) {
      throw new DuplicateEmailException(user.getEmail());
    }
  }

  private boolean checkIfEmailExists(String email, UUID id) {
    Optional<User> existingUser = userRepository.findByEmail(email);
    return existingUser.isPresent() && (id == null || !existingUser.get().getId().equals(id));
  }

  private boolean checkIfNameExists(String name, UUID id) {
    Optional<User> existingUser = userRepository.findByName(name);
    return existingUser.isPresent() && (id == null || !existingUser.get().getId().equals(id));
  }
}
