package jewellery.inventory.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogTopUpEvent;
import jewellery.inventory.aspect.annotation.LogTransferEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.TransferResourceResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.service.ProductService;
import jewellery.inventory.service.ResourceInUserService;
import jewellery.inventory.service.ResourceService;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class EventAspect {
  private final EventService eventService;
  private static final Logger logger = LoggerFactory.getLogger(EventAspect.class);

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
    Object service = proceedingJoinPoint.getTarget();
    Object oldEntity = fetchEntity(service, id, entityRequest.getClass());
    Object result = proceedingJoinPoint.proceed();
    if (oldEntity != null) {
      eventService.logEvent(eventType, result, oldEntity);
    }
    return result;
  }

  @Before("@annotation(logDeleteEvent) && args(id)")
  public void logDeletion(JoinPoint joinPoint, LogDeleteEvent logDeleteEvent, UUID id) {
    EventType eventType = logDeleteEvent.eventType();

    Object service = joinPoint.getTarget();
    Class<?> entityType = determineEntityType(service);

    Object entity = fetchEntity(service, id, entityType);
    if (entity != null) {
      eventService.logEvent(eventType, entity, null);
    }
  }

  @Before("@annotation(logDeleteEvent) && args(userId, resourceId)")
  public void logResourceInUserDeletion(
      JoinPoint joinPoint, LogDeleteEvent logDeleteEvent, UUID userId, UUID resourceId) {
    EventType eventType = logDeleteEvent.eventType();

    Object service = joinPoint.getTarget();

    Object entity = fetchEntity(service, userId);

    if (entity != null) {
      eventService.logEvent(eventType, entity, null);
    }
  }

  @AfterReturning(pointcut = "@annotation(logTopUpEvent)", returning = "result")
  public void logTopUp(JoinPoint joinPoint, LogTopUpEvent logTopUpEvent, Object result) {
    EventType eventType = logTopUpEvent.eventType();

    ResourceInUserRequestDto resourceUserDto = (ResourceInUserRequestDto) joinPoint.getArgs()[0];

    ResourcesInUserResponseDto updatedResources = (ResourcesInUserResponseDto) result;

    Map<String, Object> payload = new HashMap<>();
    payload.put("resourceUserDto", resourceUserDto);
    payload.put("updatedResources", updatedResources);

    eventService.logEvent(eventType, payload, null);
  }

  @AfterReturning(pointcut = "@annotation(logTransferEvent)", returning = "result")
  public void logTransfer(JoinPoint joinPoint, LogTransferEvent logTransferEvent, Object result) {
    EventType eventType = logTransferEvent.eventType();
    if (eventType == EventType.RESOURCE_TRANSFER && result instanceof TransferResourceResponseDto) {
      logResourceTransfer((TransferResourceResponseDto) result, eventType);
    } else if (eventType == EventType.PRODUCT_TRANSFER && result instanceof ProductResponseDto) {
      logProductTransfer((ProductResponseDto) result, eventType);
    }
  }

  private void logResourceTransfer(
      TransferResourceResponseDto transferResult, EventType eventType) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("previousOwner", transferResult.getPreviousOwner());
    payload.put("newOwner", transferResult.getNewOwner());
    payload.put("transferredResource", transferResult.getTransferredResource());

    eventService.logEvent(eventType, payload, null);
  }

  private void logProductTransfer(ProductResponseDto productResult, EventType eventType) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("newOwner", productResult.getOwner());
    payload.put("transferredProduct", productResult);

    eventService.logEvent(eventType, payload, null);
  }

  private Object fetchEntity(Object service, UUID entityId, Class<?> entityType) {
    String methodName = determineFetchMethod(service, entityType);

    try {
      Method method = service.getClass().getMethod(methodName, UUID.class);
      return method.invoke(service, entityId);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      logger.error(
          "Failed to fetch entity for logging: method={}, entityId={}", methodName, entityId, e);
      return null;
    }
  }

  private Object fetchEntity(Object service, UUID userId) {
    try {
      Method method = service.getClass().getMethod("getAllResourcesFromUser", UUID.class);
      return method.invoke(service, userId);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      logger.error("Unable to fetch entity for logging", e);
      return null;
    }
  }

  private String determineFetchMethod(Object service, Class<?> entityType) {
    try {
      if (entityType.equals(UserRequestDto.class) || service instanceof UserService) {
        return "getUser";
      } else if (ResourceRequestDto.class.isAssignableFrom(entityType)
          || service instanceof ResourceService) {
        return "getResource";
      } else if (entityType.equals(ProductRequestDto.class) || service instanceof ProductService) {
        return "getProduct";
      } else if (entityType.equals(ResourceInUserRequestDto.class)
          || service instanceof ResourceInUserService) {
        return "getAllResourcesFromUser";
      }
    } catch (IllegalArgumentException e) {
      logger.error("Unsupported entity type: {}", entityType, e);
    }
    return null;
  }

  private Class<?> determineEntityType(Object service) {
    try {
      if (service instanceof UserService) {
        return UserRequestDto.class;
      } else if (service instanceof ResourceService) {
        return ResourceRequestDto.class;
      } else if (service instanceof ProductService) {
        return ProductRequestDto.class;
      } else if (service instanceof ResourceInUserService) {
        return ResourceInUserRequestDto.class;
      }
    } catch (IllegalArgumentException e) {
      logger.error("Unsupported service type: {}", service.getClass(), e);
    }
    return null;
  }
}
