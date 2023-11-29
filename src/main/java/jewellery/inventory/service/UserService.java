package jewellery.inventory.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateEmailException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements EntityFetcher {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public List<UserResponseDto> getAllUsers() {
    return userMapper.toUserResponseList(userRepository.findAll());
  }

  public UserResponseDto getUserResponse(UUID id) {
    return userMapper.toUserResponse(getUser(id));
  }

  public User getUser(UUID id) {
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }

  @LogCreateEvent(eventType = EventType.USER_CREATE)
  public UserResponseDto createUser(UserRequestDto user) {
    User userToCreate = userMapper.toUserEntity(user);
    validateUserEmail(userToCreate);
    userToCreate.setPassword(passwordEncoder.encode(userToCreate.getPassword()));
    return userMapper.toUserResponse(userRepository.save(userToCreate));
  }

  @LogUpdateEvent(eventType = EventType.USER_UPDATE)
  public UserResponseDto updateUser(UserRequestDto userRequest, UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    User userToUpdate = userMapper.toUserEntity(userRequest);
    userToUpdate.setId(id);

    validateUserEmail(userToUpdate);

    return userMapper.toUserResponse(userRepository.save(userToUpdate));
  }

  @LogDeleteEvent(eventType = EventType.USER_DELETE)
  public void deleteUser(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    userRepository.deleteById(id);
  }

  private void validateUserEmail(User user) {
    if (isEmailUsedByOtherUser(user.getEmail(), user.getId())) {
      throw new DuplicateEmailException(user.getEmail());
    }
  }

  private boolean isEmailUsedByOtherUser(String email, UUID id) {
    Optional<User> existingUser = userRepository.findByEmail(email);
    return existingUser.isPresent() && (id == null || !existingUser.get().getId().equals(id));
  }

  @Override
  public Object fetchEntity(Object... ids) {
    ids = Arrays.stream(ids).filter(UUID.class::isInstance).toArray();
    return userMapper.toUserResponse(userRepository.findById((UUID) ids[0]).orElse(null));
  }
}
