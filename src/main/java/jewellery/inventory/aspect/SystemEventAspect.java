package jewellery.inventory.aspect;

import java.time.Instant;
import java.util.UUID;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SystemEventAspect {
  private final SystemEventRepository systemEventRepository;

  @Pointcut(
      "execution(* jewellery.inventory.service.UserService.createUser(jewellery.inventory.dto.request.UserRequestDto))")
  public void userCreationMethods() {}

  @AfterReturning(pointcut = "userCreationMethods()", returning = "createdUser")
  public void logUserCreation(JoinPoint joinPoint, UserResponseDto createdUser) {
    System.out.println("Aspect CALL");
    SystemEvent event = new SystemEvent();
    event.setTimestamp(Instant.now());
    event.setType(EventType.USER_CREATION);
    UUID newUserID = createdUser.getId();
    event.setExecutorId(newUserID);
    event.setPayload("User created with ID: " + newUserID);
    systemEventRepository.save(event);
  }
}
