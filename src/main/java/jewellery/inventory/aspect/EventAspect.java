package jewellery.inventory.aspect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class EventAspect {
  private final EventService eventService;
  private final UserService userService;
  private final UserRepository userRepository;

  @AfterReturning(
      pointcut = "@annotation(jewellery.inventory.aspect.annotation.LogCreateEvent)",
      returning = "result")
  public void logCreation(JoinPoint joinPoint, Object result) {
    eventService.logEvent(EventType.ENTITY_CREATION, result);
  }

  @Around(
      "@annotation(jewellery.inventory.aspect.annotation.LogUpdateEvent) && args(userRequest, id)")
  public Object logUpdate(
      ProceedingJoinPoint proceedingJoinPoint, UserRequestDto userRequest, UUID id)
      throws Throwable {
    UserResponseDto previousUserData = userService.fetchUserByIdAsDto(id);

    Object result = proceedingJoinPoint.proceed();

    UserResponseDto updatedUserData = (UserResponseDto) result;

    Map<String, Object> payload = new HashMap<>();
    payload.put("previousUserData", previousUserData);
    payload.put("updatedUserData", updatedUserData);

    eventService.logEvent(EventType.ENTITY_UPDATE, previousUserData);

    return result;
  }

  @Before("@annotation(jewellery.inventory.aspect.annotation.LogDeleteEvent)")
  public void logDeletion(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    UUID userId = (UUID) args[0];
    UserResponseDto user = userService.fetchUserByIdAsDto(userId);
    eventService.logEvent(EventType.ENTITY_DELETION, user);
  }
}
