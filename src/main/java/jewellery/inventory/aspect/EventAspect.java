package jewellery.inventory.aspect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogResourceQuantityRemovalEvent;
import jewellery.inventory.aspect.annotation.LogTopUpEvent;
import jewellery.inventory.aspect.annotation.LogTransferEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.UserQuantityDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.TransferResourceResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.service.SystemEventService;
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
  private final SystemEventService eventService;
  private final EntityFetchUtility entityFetchUtility;
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
    Object oldEntity = entityFetchUtility.fetchEntity(service, id, entityRequest.getClass());
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
    Class<?> entityType = entityFetchUtility.determineEntityType(service);

    Object entity = entityFetchUtility.fetchEntity(service, id, entityType);
    if (entity != null) {
      eventService.logEvent(eventType, entity, null);
    }
  }

  @Before("@annotation(logDeleteEvent) && args(userId, resourceId)")
  public void logResourceInUserDeletion(
      JoinPoint joinPoint, LogDeleteEvent logDeleteEvent, UUID userId, UUID resourceId) {
    EventType eventType = logDeleteEvent.eventType();

    Object service = joinPoint.getTarget();

    Object entity = entityFetchUtility.fetchEntity(service, userId);

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
    if (eventType == EventType.RESOURCE_TRANSFER
        && result instanceof TransferResourceResponseDto transferResourceResponseDto) {
      eventService.logResourceTransfer(transferResourceResponseDto, eventType);
    } else if (eventType == EventType.PRODUCT_TRANSFER
        && result instanceof ProductResponseDto productResponseDto) {
      eventService.logProductTransfer(productResponseDto, eventType);
    }
  }

  @Around("@annotation(logResourceQuantityRemovalEvent)")
  public Object logResourceQuantityRemoval(
      ProceedingJoinPoint proceedingJoinPoint,
      LogResourceQuantityRemovalEvent logResourceQuantityRemovalEvent)
      throws Throwable {

    UUID userId = (UUID) proceedingJoinPoint.getArgs()[0];
    UUID resourceId = (UUID) proceedingJoinPoint.getArgs()[1];

    ResourceOwnedByUsersResponseDto resourceInUserBefore = fetchResourceInUser(resourceId);
    Object result = proceedingJoinPoint.proceed();
    ResourceOwnedByUsersResponseDto resourceInUserAfter = fetchResourceInUser(resourceId);

    Map<String, Object> payload =
        buildResourceQuantityRemovalLogPayload(resourceInUserBefore, resourceInUserAfter, userId);

    EventType eventType = logResourceQuantityRemovalEvent.eventType();
    eventService.logEvent(eventType, payload, null);

    return result;
  }

  private ResourceOwnedByUsersResponseDto fetchResourceInUser(UUID resourceId) {
    return entityFetchUtility.fetchResourceInUser(resourceId);
  }

  private Map<String, Object> buildResourceQuantityRemovalLogPayload(
      ResourceOwnedByUsersResponseDto before, ResourceOwnedByUsersResponseDto after, UUID userId) {

    Map<String, Object> payload = new HashMap<>();

    UserQuantityDto userQuantityBefore = findUserQuantityDto(before, userId);
    UserQuantityDto userQuantityAfter = findUserQuantityDto(after, userId);

    if (userQuantityBefore != null && userQuantityAfter != null) {
      double quantityRemoved = userQuantityBefore.getQuantity() - userQuantityAfter.getQuantity();
      payload.put("QuantityRemoved", quantityRemoved);
      payload.put("resourceInUserBefore", withSingleUserQuantityDto(before, userQuantityBefore));
      payload.put("resourceInUserAfter", withSingleUserQuantityDto(after, userQuantityAfter));
    } else {
      logger.error("User data not found");
    }

    return payload;
  }

  private UserQuantityDto findUserQuantityDto(
      ResourceOwnedByUsersResponseDto resourceOwnedByUsersResponseDto, UUID userId) {

    return resourceOwnedByUsersResponseDto.getUsersAndQuantities().stream()
        .filter(uq -> uq.getOwner().getId().equals(userId))
        .findFirst()
        .orElse(null);
  }

  private ResourceOwnedByUsersResponseDto withSingleUserQuantityDto(
      ResourceOwnedByUsersResponseDto resourceOwnedByUsersResponseDto,
      UserQuantityDto userQuantityDto) {

    resourceOwnedByUsersResponseDto.setUsersAndQuantities(List.of(userQuantityDto));
    return resourceOwnedByUsersResponseDto;
  }
}
