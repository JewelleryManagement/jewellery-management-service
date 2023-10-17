package jewellery.inventory.aspect;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventService {

  private final SystemEventRepository systemEventRepository;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public void logUserCreation(UserResponseDto createdUser) {
    logEvent(EventType.USER_CREATION, buildPayloadForCreatedUser(createdUser), createdUser.getId());
  }

  public void logUserDeletion(JoinPoint joinPoint, UUID userId) {
    UserResponseDto user = fetchUserByIdAsDto(userId);
    logEvent(EventType.USER_DELETION, buildPayloadForDeletedUser(user), userId);
  }

  public UserResponseDto logUserUpdate(
      ProceedingJoinPoint joinPoint, UserRequestDto userRequest, UUID id) throws Throwable {
    UserResponseDto previousUserDto = fetchUserByIdAsDto(id);

    UserResponseDto updatedUserResponse = (UserResponseDto) joinPoint.proceed();

    logEvent(
        EventType.ENTITY_UPDATE,
        buildPayloadForUpdatedUser(previousUserDto, updatedUserResponse),
        id);
    return updatedUserResponse;
  }

  protected UserResponseDto fetchUserByIdAsDto(UUID id) {
    User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    return userMapper.toUserResponse(user);
  }

  protected void logEvent(EventType type, Map<String, Object> payload, UUID executorId) {
    SystemEvent event = new SystemEvent();
    event.setTimestamp(Instant.now());
    event.setType(type);
    event.setExecutorId(executorId);
    event.setPayload(payload);
    systemEventRepository.save(event);
  }

  protected Map<String, Object> buildPayloadForUser(
      String message, UUID userId, String userName, String userEmail) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("message", message);
    payload.put("timestamp", formatCurrentTimestamp());
    payload.put("userId", userId);
    payload.put("userName", userName);
    payload.put("userEmail", userEmail);
    return payload;
  }

  protected Map<String, Object> buildPayloadForCreatedUser(UserResponseDto user) {
    return buildPayloadForUser(
        "User with ID: " + user.getId() + " was created!",
        user.getId(),
        user.getName(),
        user.getEmail());
  }

  protected Map<String, Object> buildPayloadForDeletedUser(UserResponseDto user) {
    return buildPayloadForUser(
        "User with ID: " + user.getId() + " was deleted ",
        user.getId(),
        user.getName(),
        user.getEmail());
  }

  protected Map<String, Object> buildPayloadForUpdatedUser(
      UserResponseDto previousUserDto, UserResponseDto updatedUserResponse) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("message", "User with ID: " + updatedUserResponse.getId() + " updated");
    payload.put("timestamp", formatCurrentTimestamp());
    payload.put("userId", updatedUserResponse.getId());
    payload.put("userNameBeforeUpdate", previousUserDto != null ? previousUserDto.getName() : null);
    payload.put("userNameAfterUpdate", updatedUserResponse.getName());
    payload.put(
        "userEmailBeforeUpdate", previousUserDto != null ? previousUserDto.getEmail() : null);
    payload.put("userEmailAfterUpdate", updatedUserResponse.getEmail());
    return payload;
  }

  protected String formatCurrentTimestamp() {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'at' HH:mm:ss 'on' dd.MM.yyyy");
    return localDateTime.format(formatter);
  }
}
