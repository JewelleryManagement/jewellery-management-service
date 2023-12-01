package jewellery.inventory.aspect;

import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.model.EventType;
import jewellery.inventory.service.SystemEventService;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
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
  private static final Logger logger = Logger.getLogger(EventAspect.class);

  private final SystemEventService eventService;

  @AfterReturning(pointcut = "@annotation(logCreateEvent)", returning = "result")
  public void logCreation(LogCreateEvent logCreateEvent, Object result) {
    EventType eventType = logCreateEvent.eventType();
    eventService.logEvent(eventType, result);
  }

  @Around("@annotation(logUpdateEvent)")
  public Object logUpdate(ProceedingJoinPoint proceedingJoinPoint, LogUpdateEvent logUpdateEvent)
      throws Throwable {
    EventType eventType = logUpdateEvent.eventType();
    Object service = proceedingJoinPoint.getTarget();

    if (!(service instanceof EntityFetcher entityFetcher)) {
      logger.error("Service does not implement EntityFetcher");
      return null;
    }

    Object oldEntity = entityFetcher.fetchEntity(proceedingJoinPoint.getArgs());
    Object newEntity = proceedingJoinPoint.proceed();

    eventService.logEvent(eventType, newEntity, oldEntity);

    return newEntity;
  }

  @Before("@annotation(logDeleteEvent) && args(id)")
  public void logDeletion(JoinPoint joinPoint, LogDeleteEvent logDeleteEvent, UUID id) {
    EventType eventType = logDeleteEvent.eventType();
    Object service = joinPoint.getTarget();

    if (!(service instanceof EntityFetcher entityFetcher)) {
      logger.error(
          "Service: {"
              + service.getClass()
              + "} does not implement EntityFetcher for deletion logging");
      return;
    }

    Object entityBeforeDeletion = entityFetcher.fetchEntity(id);

    if (entityBeforeDeletion != null) {
      eventService.logEvent(eventType, entityBeforeDeletion);
    } else {
      logger.error("Entity with id: {" + id + "} not found for deletion logging");
    }
  }
}
