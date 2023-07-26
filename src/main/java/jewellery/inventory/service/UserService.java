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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public List<UserResponse> getAllUsers() {
    return UserMapper.INSTANCE.toUserResponseList(userRepository.findAll());
  }

  public UserResponse getUser(UUID id) {
    return UserMapper.INSTANCE.toUserResponse(
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
  }

  public UserResponse createUser(UserRequest user) {
    User userToCreate = UserMapper.INSTANCE.toUserEntity(user);
    validateUserDetails(userToCreate);
    return UserMapper.INSTANCE.toUserResponse(userRepository.save(userToCreate));
  }

  public UserResponse updateUser(UserRequest userRequest, UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    User userToUpdate = UserMapper.INSTANCE.toUserEntity(userRequest);
    userToUpdate.setId(id);

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
    if (isNameUsedByOtherUser(user.getName(), user.getId())) {
      throw new DuplicateNameException(user.getName());
    }

    if (isEmailUsedByOtherUser(user.getEmail(), user.getId())) {
      throw new DuplicateEmailException(user.getEmail());
    }
  }

  private boolean isEmailUsedByOtherUser(String email, UUID id) {
    Optional<User> existingUser = userRepository.findByEmail(email);
    return existingUser.isPresent() && (id == null || !existingUser.get().getId().equals(id));
  }

  private boolean isNameUsedByOtherUser(String name, UUID id) {
    Optional<User> existingUser = userRepository.findByName(name);
    return existingUser.isPresent() && (id == null || !existingUser.get().getId().equals(id));
  }
}
