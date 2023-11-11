package jewellery.inventory.aspect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogResourceQuantityRemovalEvent;
import jewellery.inventory.aspect.annotation.LogTopUpEvent;
import jewellery.inventory.aspect.annotation.LogTransferEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.TransferResourceResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.mapper.ResourcesInUserMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
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
  private static final Logger logger = LoggerFactory.getLogger(EventAspect.class);
  private final SystemEventService eventService;
  private final ResourcesInUserMapper resourcesInUserMapper;

  @AfterReturning(pointcut = "@annotation(logCreateEvent)", returning = "result")
  public void logCreation(LogCreateEvent logCreateEvent, Object result) {
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

    if (!(service instanceof EntityFetcher entityFetcher)) {
      logger.error("Service does not implement EntityFetcher");
      return null;
    }

    Object oldEntity = entityFetcher.fetchEntity(id);
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

    if (!(service instanceof EntityFetcher entityFetcher)) {
      logger.error("Service does not implement EntityFetcher for deletion logging");
      return;
    }

    Object entityBeforeDeletion = entityFetcher.fetchEntity(id);

    if (entityBeforeDeletion != null) {
      eventService.logEvent(eventType, entityBeforeDeletion, null);
    } else {
      logger.error("Entity not found for deletion logging");
    }
  }

  @Before("@annotation(logDeleteEvent) && args(userId, resourceId)")
  public void logResourceInUserDeletion(
      JoinPoint joinPoint, LogDeleteEvent logDeleteEvent, UUID userId, UUID resourceId) {
    if (!(joinPoint.getTarget() instanceof EntityFetcher entityFetcher)) {
      logger.error(
          "Service does not implement EntityFetcher interface. Unable to log deletion event.");
      return;
    }

    EventType eventType = logDeleteEvent.eventType();
    ResourcesInUserResponseDto userResourcesBeforeDeletion =
        fetchUserResources(entityFetcher, userId, resourceId);

    if (userResourcesBeforeDeletion != null) {
      eventService.logEvent(eventType, userResourcesBeforeDeletion, null);
    }
  }

  @Around("@annotation(logTopUpEvent)")
  public Object logResourceTopUp(
      ProceedingJoinPoint proceedingJoinPoint, LogTopUpEvent logTopUpEvent) throws Throwable {

    EventType eventType = logTopUpEvent.eventType();
    Object service = proceedingJoinPoint.getTarget();

    if (!(service instanceof EntityFetcher entityFetcher)) {
      logger.error("Service does not implement EntityFetcher");
      return proceedingJoinPoint.proceed();
    }
    ResourceInUserRequestDto resourceUserDto =
        (ResourceInUserRequestDto) proceedingJoinPoint.getArgs()[0];

    ResourcesInUserResponseDto oldResources =
        fetchAndFilterResourceState(
            entityFetcher, resourceUserDto.getUserId(), resourceUserDto.getResourceId());

    ResourcesInUserResponseDto updatedResources =
        (ResourcesInUserResponseDto) proceedingJoinPoint.proceed();

    Map<String, Object> payload = new HashMap<>();
    payload.put("quantityAdded", resourceUserDto.getQuantity());
    payload.put("oldResources", oldResources);
    payload.put("updatedResources", updatedResources);

    eventService.logEvent(eventType, payload, null);
    return updatedResources;
  }

  @AfterReturning(pointcut = "@annotation(logTransferEvent)", returning = "result")
  public void logTransfer(LogTransferEvent logTransferEvent, Object result) {
    EventType eventType = logTransferEvent.eventType();
    if (result instanceof TransferResourceResponseDto || result instanceof ProductResponseDto) {
      eventService.logEvent(eventType, result, null);
    }
  }

  @Around("@annotation(logResourceQuantityRemovalEvent)")
  public Object logResourceQuantityRemoval(
      ProceedingJoinPoint proceedingJoinPoint,
      LogResourceQuantityRemovalEvent logResourceQuantityRemovalEvent)
      throws Throwable {

    if (!(proceedingJoinPoint.getTarget() instanceof EntityFetcher entityFetcher)) {
      logger.error("Service does not implement EntityFetcher interface. Unable to log event.");
      return proceedingJoinPoint.proceed();
    }

    UUID userId = (UUID) proceedingJoinPoint.getArgs()[0];
    UUID resourceId = (UUID) proceedingJoinPoint.getArgs()[1];

    ResourcesInUserResponseDto beforeDto =
        fetchAndFilterResourceState(entityFetcher, userId, resourceId);
    if (beforeDto == null) {
      logStateNullWarning("before", userId, resourceId);
      return proceedingJoinPoint.proceed();
    }

    Object result = proceedingJoinPoint.proceed();

    ResourcesInUserResponseDto afterDto =
        fetchAndFilterResourceState(entityFetcher, userId, resourceId);
    if (afterDto == null) {
      logStateNullWarning("after", userId, resourceId);
      return result;
    }

    eventService.logEvent(logResourceQuantityRemovalEvent.eventType(), afterDto, beforeDto);
    return result;
  }

  private ResourcesInUserResponseDto filterResourcesInUserResponseDto(User owner, UUID resourceId) {
    ResourcesInUserResponseDto dto = resourcesInUserMapper.toResourcesInUserResponseDto(owner);
    List<ResourceQuantityResponseDto> filteredResources =
        dto.getResourcesAndQuantities().stream()
            .filter(r -> r.getResource().getId().equals(resourceId))
            .collect(Collectors.toList());
    dto.setResourcesAndQuantities(filteredResources);
    return dto;
  }

  private ResourcesInUserResponseDto fetchAndFilterResourceState(
      EntityFetcher entityFetcher, UUID userId, UUID resourceId) {
    ResourceInUser resourceEntity = (ResourceInUser) entityFetcher.fetchEntity(userId, resourceId);
    if (resourceEntity == null) {
      return null;
    }
    return filterResourcesInUserResponseDto(resourceEntity.getOwner(), resourceId);
  }

  private ResourcesInUserResponseDto fetchUserResources(
      EntityFetcher entityFetcher, UUID userId, UUID resourceId) {
    ResourceInUser resourceInUser = (ResourceInUser) entityFetcher.fetchEntity(userId, resourceId);
    if (resourceInUser == null) {
      logger.warn(
          "Entity before deletion is null. Unable to log deletion for userId: {}, resourceId: {}",
          userId,
          resourceId);
      return null;
    }

    return resourcesInUserMapper.toResourcesInUserResponseDto(resourceInUser.getOwner());
  }

  private void logStateNullWarning(String state, UUID userId, UUID resourceId) {
    logger.warn(
        "ResourceInUser {} state is null. Changes will not be logged for userId: {}, resourceId: {}",
        state,
        userId,
        resourceId);
  }
}
