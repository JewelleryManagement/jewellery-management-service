package jewellery.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateEmailException;
import jewellery.inventory.exception.duplicate.DuplicateNameException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public List<UserResponseDto> getAllUsers() {
    return UserMapper.INSTANCE.toUserResponseList(userRepository.findAll());
  }

  public UserResponseDto getUser(UUID id) {
    return UserMapper.INSTANCE.toUserResponse(
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
  }

  public UserResponseDto createUser(UserRequestDto user) {
    User userToCreate = UserMapper.INSTANCE.toUserEntity(user);
    validateUserEmailAndName(userToCreate);
    return UserMapper.INSTANCE.toUserResponse(userRepository.save(userToCreate));
  }

  public UserResponseDto updateUser(UserRequestDto userRequest, UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    User userToUpdate = UserMapper.INSTANCE.toUserEntity(userRequest);
    userToUpdate.setId(id);

    validateUserEmailAndName(userToUpdate);

    return UserMapper.INSTANCE.toUserResponse(userRepository.save(userToUpdate));
  }

  public void deleteUser(UUID id) {
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
    } else {
      throw new UserNotFoundException(id);
    }
  }

  private void validateUserEmailAndName(User user) {
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
