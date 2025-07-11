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
import jewellery.inventory.dto.request.UserUpdateRequestDto;
import jewellery.inventory.dto.response.DetailedUserResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateEmailException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public List<DetailedUserResponseDto> getAllUsers() {
    logger.debug("Fetching all users.");
    return userMapper.toDetailedUserResponseList(userRepository.findAll());
  }

  public DetailedUserResponseDto getUserResponse(UUID id) {
    return userMapper.toDetailedUserResponse(getUser(id));
  }

  public User getUser(UUID id) {
    logger.debug("Fetching user with ID: {}", id);
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }

  public User saveUser(User user) {
    return userRepository.save(user);
  }

  @LogCreateEvent(eventType = EventType.USER_CREATE)
  public DetailedUserResponseDto createUser(UserRequestDto user) {
    User userToCreate = userMapper.toUserEntity(user);
    validateUserEmail(userToCreate);
    userToCreate.setPassword(passwordEncoder.encode(userToCreate.getPassword()));
    logger.info("Create new User");
    return userMapper.toDetailedUserResponse(saveUser(userToCreate));
  }

  @LogUpdateEvent(eventType = EventType.USER_UPDATE)
  public DetailedUserResponseDto updateUser(UserUpdateRequestDto userRequest, UUID id) {
    User oldUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

    User userToUpdate = userMapper.toUserEntity(userRequest);
    userToUpdate.setPassword(oldUser.getPassword());
    userToUpdate.setId(id);

    validateUserEmail(userToUpdate);
    logger.info("User with ID: {} updated successfully.", id);
    return userMapper.toDetailedUserResponse(saveUser(userToUpdate));
  }

  @LogDeleteEvent(eventType = EventType.USER_DELETE)
  public void deleteUser(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    logger.info("Deleting user with ID: {}", id);
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
    return userMapper.toDetailedUserResponse(userRepository.findById((UUID) ids[0]).orElse(null));
  }
}
