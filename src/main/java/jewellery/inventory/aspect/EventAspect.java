package jewellery.inventory.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.model.EventType;
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

  @AfterReturning(pointcut = "@annotation(logCreateEvent)", returning = "result")
  public void logCreation(JoinPoint joinPoint, LogCreateEvent logCreateEvent, Object result) {
    EventType eventType = logCreateEvent.eventType();
    eventService.logEvent(eventType, result, null);
  }

  @Around("@annotation(logUpdateEvent) && args(entityRequest, id)")
  public Object logUpdate(
      ProceedingJoinPoint proceedingJoinPoint,
      LogUpdateEvent logUpdateEvent,
      Object entityRequest,
      UUID id)
      throws Throwable {
    EventType eventType = logUpdateEvent.eventType();
    Object oldEntity = eventService.fetchEntityByIdAsDto(id, entityRequest.getClass());
    Object result = proceedingJoinPoint.proceed();

    eventService.logEvent(eventType, result, oldEntity);

    return result;
  }

  @Before("@annotation(logDeleteEvent)")
  public void logDeletion(JoinPoint joinPoint, LogDeleteEvent logDeleteEvent) {
    EventType eventType = logDeleteEvent.eventType();
    Object[] args = joinPoint.getArgs();
    UUID entityId = (UUID) args[0];

    Object entity = fetchEntity(joinPoint, entityId);
    if (entity != null) {
      eventService.logEvent(eventType, entity, null);
    }
  }

  private Object fetchEntity(JoinPoint joinPoint, UUID entityId) {
    Object service = joinPoint.getTarget();
    try {
      Method method = service.getClass().getMethod("fetchByIdAsDto", UUID.class);
      return method.invoke(service, entityId);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      // Handle exceptions as necessary
      e.printStackTrace();
    }
    return null;
  }
}
