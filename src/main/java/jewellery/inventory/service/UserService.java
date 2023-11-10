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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public List<UserResponseDto> getAllUsers() {
    return userMapper.toUserResponseList(userRepository.findAll());
  }

  public UserResponseDto getUserResponse(UUID id) {
    return userMapper.toUserResponse(
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
  }

  public User getUser(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }


  public UserResponseDto createUser(UserRequestDto user) {
    User userToCreate = userMapper.toUserEntity(user);
    validateUserEmailAndName(userToCreate);
    userToCreate.setPassword(passwordEncoder.encode(userToCreate.getPassword()));
    return userMapper.toUserResponse(userRepository.save(userToCreate));
  }

  public UserResponseDto updateUser(UserRequestDto userRequest, UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    User userToUpdate = userMapper.toUserEntity(userRequest);
    userToUpdate.setId(id);

    validateUserEmailAndName(userToUpdate);

    return userMapper.toUserResponse(userRepository.save(userToUpdate));
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
