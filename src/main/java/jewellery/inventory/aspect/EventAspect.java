package jewellery.inventory.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
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

  @AfterReturning(
      pointcut = "@annotation(jewellery.inventory.aspect.annotation.LogCreateEvent)",
      returning = "result")
  public void logCreation(JoinPoint joinPoint, Object result) throws Exception {
    eventService.logEvent(EventType.ENTITY_CREATION, result, null);
  }

  @Around(
      "@annotation(jewellery.inventory.aspect.annotation.LogUpdateEvent) && args(entityRequest, id)")
  public Object logUpdate(ProceedingJoinPoint proceedingJoinPoint, Object entityRequest, UUID id)
      throws Throwable {

    Object oldEntity = eventService.fetchEntityByIdAsDto(id, entityRequest.getClass());
    Object result = proceedingJoinPoint.proceed();

    eventService.logEvent(EventType.ENTITY_UPDATE, result, oldEntity);

    return result;
  }

  @Before("@annotation(jewellery.inventory.aspect.annotation.LogDeleteEvent)")
  public void logDeletion(JoinPoint joinPoint) throws Exception {
    Object[] args = joinPoint.getArgs();
    UUID entityId = (UUID) args[0];

    Object entity = fetchEntity(joinPoint, entityId);
    if (entity != null) {
      eventService.logEvent(EventType.ENTITY_DELETION, entity, null);
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
