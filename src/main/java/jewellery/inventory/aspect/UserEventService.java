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
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.security.JwtUtils;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

//@Component
//@RequiredArgsConstructor
//public class UserEventService {
//
//  private final SystemEventRepository systemEventRepository;
//  private final UserService userService;
//  private final JwtUtils jwtUtils;
//
//  public void logUserCreation(UserResponseDto createdUser) {
//    UUID executorId = jwtUtils.getCurrentUserId();
//    logEvent(EventType.ENTITY_CREATION, buildPayloadForCreatedUser(createdUser), executorId);
//  }
//
//  public void logUserDeletion(JoinPoint joinPoint, UUID userId) {
//    UUID executorId = jwtUtils.getCurrentUserId();
//    UserResponseDto user = userService.fetchUserByIdAsDto(userId);
//    logEvent(EventType.ENTITY_DELETION, buildPayloadForDeletedUser(user), executorId);
//  }
//
//  public UserResponseDto logUserUpdate(
//      ProceedingJoinPoint joinPoint, UserRequestDto userRequest, UUID id) throws Throwable {
//    UserResponseDto previousUserDto = userService.fetchUserByIdAsDto(id);
//
//    UserResponseDto updatedUserResponse = (UserResponseDto) joinPoint.proceed();
//
//    logEvent(
//        EventType.ENTITY_UPDATE,
//        buildPayloadForUpdatedUser(previousUserDto, updatedUserResponse),
//        id);
//    return updatedUserResponse;
//  }
//
//  protected void logEvent(EventType type, Map<String, Object> payload, UUID executorId) {
//
//    SystemEvent event = new SystemEvent();
//    event.setTimestamp(Instant.now());
//    event.setType(type);
//    event.setExecutorId(executorId);
//    event.setPayload(payload);
//    systemEventRepository.save(event);
//  }
//
//  protected Map<String, Object> buildPayloadForUser(
//      String message, UUID userId, String userName, String userEmail) {
//    Map<String, Object> payload = new HashMap<>();
//    payload.put("message", message);
//    payload.put("timestamp", formatCurrentTimestamp());
//    payload.put("userId", userId);
//    payload.put("userName", userName);
//    payload.put("userEmail", userEmail);
//    return payload;
//  }
//
//  protected Map<String, Object> buildPayloadForCreatedUser(UserResponseDto user) {
//    return buildPayloadForUser(
//        "User with ID: " + user.getId() + " was created!",
//        user.getId(),
//        user.getName(),
//        user.getEmail());
//  }
//
//  protected Map<String, Object> buildPayloadForDeletedUser(UserResponseDto user) {
//    return buildPayloadForUser(
//        "User with ID: " + user.getId() + " was deleted ",
//        user.getId(),
//        user.getName(),
//        user.getEmail());
//  }
//
//  protected Map<String, Object> buildPayloadForUpdatedUser(
//      UserResponseDto previousUserDto, UserResponseDto updatedUserResponse) {
//    Map<String, Object> payload = new LinkedHashMap<>();
//    payload.put("message", "User with ID: " + updatedUserResponse.getId() + " updated");
//    payload.put("timestamp", formatCurrentTimestamp());
//    payload.put("userId", updatedUserResponse.getId());
//    payload.put("userNameBeforeUpdate", previousUserDto != null ? previousUserDto.getName() : null);
//    payload.put("userNameAfterUpdate", updatedUserResponse.getName());
//    payload.put(
//        "userEmailBeforeUpdate", previousUserDto != null ? previousUserDto.getEmail() : null);
//    payload.put("userEmailAfterUpdate", updatedUserResponse.getEmail());
//    return payload;
//  }
//
//  protected String formatCurrentTimestamp() {
//    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'at' HH:mm:ss 'on' dd.MM.yyyy");
//    return localDateTime.format(formatter);
//  }
//}
