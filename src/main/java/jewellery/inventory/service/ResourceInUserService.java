package jewellery.inventory.service;

import static jewellery.inventory.model.EventType.RESOURCE_REMOVE_QUANTITY;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.*;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.ResourcePurchaseRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.TransferResourceResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.mapper.ResourcesInUserMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceInUserService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(ResourceInUserService.class);
  private final UserRepository userRepository;
  private final ResourceRepository resourceRepository;
  private final ResourceInUserRepository resourceInUserRepository;
  private final ResourcesInUserMapper resourcesInUserMapper;
  private final UserMapper userMapper;
  private final ResourceMapper resourceMapper;
  private static final BigDecimal EPSILON = new BigDecimal("1e-10");

  @Transactional
  @LogCreateEvent(eventType = EventType.RESOURCE_TRANSFER)
  public TransferResourceResponseDto transferResources(
      TransferResourceRequestDto transferResourceRequestDto) {

    User previousOwner = findUserById(transferResourceRequestDto.getPreviousOwnerId());
    User newOwner = findUserById(transferResourceRequestDto.getNewOwnerId());
    ResourceInUser resourceInPreviousOwner =
        findResourceInUserOrThrow(
            previousOwner, transferResourceRequestDto.getTransferredResourceId());

    removeQuantityFromResource(resourceInPreviousOwner, transferResourceRequestDto.getQuantity());
    addResourceToUser(
        newOwner, resourceInPreviousOwner.getResource(), transferResourceRequestDto.getQuantity());

    TransferResourceResponseDto transferResourceResponseDto =
        getTransferResourceResponseDto(
            previousOwner,
            newOwner,
            resourceInPreviousOwner.getResource(),
            transferResourceRequestDto.getQuantity());
    logger.info("Transfer completed successfully {}", transferResourceResponseDto);
    return transferResourceResponseDto;
  }

  @Transactional
  @LogUpdateEvent(eventType = EventType.RESOURCE_ADD_QUANTITY)
  public ResourcesInUserResponseDto addResourceToUser(ResourcePurchaseRequestDto requestDto) {
    return purchaseResource(requestDto);
  }

  public void addResourceToUserNoLog(ResourceInUserRequestDto resourceUserDto) {
    User user = findUserById(resourceUserDto.getUserId());
    Resource resource = findResourceById(resourceUserDto.getResourceId());

    resourcesInUserMapper.toResourcesInUserResponseDto(
        addResourceToUser(user, resource, resourceUserDto.getQuantity()));
  }

  public ResourcesInUserResponseDto getAllResourcesFromUser(UUID userId) {
    User user = findUserById(userId);
    return resourcesInUserMapper.toResourcesInUserResponseDto(user);
  }

  @Transactional
  @LogUpdateEvent(eventType = RESOURCE_REMOVE_QUANTITY)
  public ResourcesInUserResponseDto removeQuantityFromResource(
      UUID userId, UUID resourceId, BigDecimal quantity) {
    return removeQuantityFromResourceNoLog(userId, resourceId, quantity);
  }

  public ResourcesInUserResponseDto removeQuantityFromResourceNoLog(
      UUID userId, UUID resourceId, BigDecimal quantity) {
    User user = findUserById(userId);
    ResourceInUser resourceInUser = findResourceInUserOrThrow(user, resourceId);
    removeQuantityFromResource(resourceInUser, quantity);

    if (resourceInUser != null) {
      return resourcesInUserMapper.toResourcesInUserResponseDto(resourceInUser);
    }
    return new ResourcesInUserResponseDto();
  }

  @Transactional
  @LogUpdateEvent(eventType = RESOURCE_REMOVE_QUANTITY)
  public void removeResourceFromUser(UUID userId, UUID resourceId) {
    logger.info("Removing resource from user. UserId: {}, ResourceId: {}", userId, resourceId);
    User user = findUserById(userId);
    ResourceInUser resourceToRemove = findResourceInUserOrThrow(user, resourceId);

    user.getResourcesOwned().remove(resourceToRemove);
    logger.debug("Resource to remove: {}", resourceToRemove);
    userRepository.save(user);
  }

  public ResourceOwnedByUsersResponseDto getUsersAndQuantities(UUID resourceId) {
    Resource resource = findResourceById(resourceId);
    return resourcesInUserMapper.toResourcesOwnedByUsersResponseDto(resource);
  }

  private ResourceInUser getResourceInUser(UUID userId, UUID resourceId) {
    User user = findUserById(userId);
    logger.debug("Getting resource in user. UserId: {}, ResourceId: {}", userId, resourceId);
    return findResourceInUser(user, resourceId).orElse(null);
  }

  private ResourcesInUserResponseDto getResourceInUserResponse(UUID userId, UUID resourceId) {
    ResourceInUser resourceInUser = getResourceInUser(userId, resourceId);
    if (resourceInUser != null) {
      return resourcesInUserMapper.toResourcesInUserResponseDto(resourceInUser);
    }
    return null;
  }

  private ResourceInUser getResourceInUser(User user, Resource resource) {
    logger.debug("Getting resource in user. User: {}, Resource: {}", user, resource);
    return findResourceInUser(user, resource.getId())
        .orElseGet(() -> createAndAddNewResourceInUser(user, resource, BigDecimal.ZERO));
  }

  private ResourceInUser addResourceToUser(User user, Resource resource, BigDecimal quantity) {
    logger.info(
        "Adding resource to user. User: {}, Resource: {}, Quantity: {}", user, resource, quantity);
    ResourceInUser resourceInUser = getResourceInUser(user, resource);
    resourceInUser.setQuantity(resourceInUser.getQuantity().add(quantity));
    resourceInUserRepository.save(resourceInUser);
    logger.debug("ResourceInUser after addition: {}", resourceInUser);
    return resourceInUser;
  }

  private ResourceInUser removeQuantityFromResource(
      ResourceInUser resourceInUser, BigDecimal quantityToRemove) {

    BigDecimal totalQuantity = resourceInUser.getQuantity();
    BigDecimal newQuantity = totalQuantity.subtract(quantityToRemove);

    if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
      throw new InsufficientResourceQuantityException(quantityToRemove, totalQuantity);
    }

    resourceInUser.setQuantity(newQuantity);
    User owner = resourceInUser.getOwner();
    if (newQuantity.abs().compareTo(EPSILON) < 0) {
      owner.getResourcesOwned().remove(resourceInUser);
      resourceInUser = null;
    }
    userRepository.save(owner);
    logger.debug("ResourceInUser after quantity removal: {}", resourceInUser);
    return resourceInUser;
  }

  private ResourceInUser findResourceInUserOrThrow(User previousOwner, UUID resourceId) {
    return findResourceInUser(previousOwner, resourceId)
        .orElseThrow(() -> new ResourceInUserNotFoundException(resourceId, previousOwner.getId()));
  }

  private User findUserById(UUID userId) {
    logger.info("Finding user by ID: {}", userId);
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }

  private Resource findResourceById(UUID resourceId) {
    logger.info("Finding resource by ID: {}", resourceId);
    return resourceRepository
        .findById(resourceId)
        .orElseThrow(() -> new ResourceNotFoundException(resourceId));
  }

  private Optional<ResourceInUser> findResourceInUser(User user, UUID resourceId) {
    logger.info("Finding resource by ID: {}, Finding user by ID: {}", resourceId, user.getId());
    return user.getResourcesOwned().stream()
        .filter(r -> r.getResource().getId().equals(resourceId))
        .findFirst();
  }

  private ResourceInUser createAndAddNewResourceInUser(
      User user, Resource resource, BigDecimal quantity) {
    ResourceInUser resourceInUser = new ResourceInUser();
    resourceInUser.setOwner(user);
    resourceInUser.setResource(resource);
    resourceInUser.setQuantity(quantity);
    user.getResourcesOwned().add(resourceInUser);
    logger.info("New resource in user created: {}", resourceInUser);
    return resourceInUser;
  }

  private TransferResourceResponseDto getTransferResourceResponseDto(
      User previousOwner, User newOwner, Resource resource, BigDecimal quantity) {

    TransferResourceResponseDto responseDto =
        TransferResourceResponseDto.builder()
            .previousOwner(userMapper.toUserResponse(previousOwner))
            .newOwner(userMapper.toUserResponse(newOwner))
            .transferredResource(
                ResourceQuantityResponseDto.builder()
                    .resource(resourceMapper.toResourceResponse(resource))
                    .quantity(quantity)
                    .build())
            .build();

    logger.info("TransferResourceResponseDto created: {}", responseDto);
    return responseDto;
  }

  @Override
  public Object fetchEntity(Object... ids) {
    if (ids != null && ids.length > 0) {
      if (ids[0] instanceof ResourceInUserRequestDto resourceInUserRequestDto) {

        return getResourceInUserResponse(
            resourceInUserRequestDto.getUserId(), resourceInUserRequestDto.getResourceId());
      } else {
        return getResourceInUserResponse((UUID) ids[0], (UUID) ids[1]);
      }
    }
    return null;
  }

  private ResourcesInUserResponseDto purchaseResource(ResourcePurchaseRequestDto requestDto) {
    User user = findUserById(requestDto.getUserId());
    Resource resource = findResourceById(requestDto.getResourceId());
    ResourceInUser resourceInUser = addResourceToUser(user, resource, requestDto.getQuantity());
    resourceInUser.setDealPrice(requestDto.getDealPrice());

    ResourcesInUserResponseDto responseDto =
        resourcesInUserMapper.toResourcePurchaseResponse(resourceInUser);
    logger.info("Resource purchase completed successfully. Response: {}", responseDto);
    return responseDto;
  }
}
