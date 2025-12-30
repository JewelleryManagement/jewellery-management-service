package jewellery.inventory.service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.ResourcePurchaseRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.mapper.ResourcesInUserMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import jewellery.inventory.repository.ResourceInUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceInUserService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(ResourceInUserService.class);
  private final ResourceInUserRepository resourceInUserRepository;
  private final ResourcesInUserMapper resourcesInUserMapper;
  private final UserMapper userMapper;
  private final ResourceMapper resourceMapper;
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private final UserService userService;
  private final ResourceService resourceService;
  private static final BigDecimal EPSILON = new BigDecimal("1e-10");

  public void addResourceToUserNoLog(ResourceInUserRequestDto resourceUserDto) {
    User user = userService.getUser(resourceUserDto.getUserId());
    Resource resource = findResourceById(resourceUserDto.getResourceId());

    resourcesInUserMapper.toResourcesInUserResponseDto(
        addResourceToUser(user, resource, resourceUserDto.getQuantity()));
  }

  public ResourcesInUserResponseDto getAllResourcesFromUser(UUID userId) {
    User user = userService.getUser(userId);
    return resourcesInUserMapper.toResourcesInUserResponseDto(user);
  }

  public ResourcesInUserResponseDto removeQuantityFromResourceNoLog(
      UUID userId, UUID resourceId, BigDecimal quantity) {
    User user = userService.getUser(userId);
    ResourceInUser resourceInUser = findResourceInUserOrThrow(user, resourceId);
    removeQuantityFromResource(resourceInUser, quantity);

    if (resourceInUser != null) {
      return resourcesInUserMapper.toResourcesInUserResponseDto(resourceInUser);
    }
    return new ResourcesInUserResponseDto();
  }

  public List<PurchasedResourceQuantityResponseDto> getAllPurchasedResources(UUID userId) {
    User user = userService.getUser(userId);
    return purchasedResourceInUserRepository.findAllByOwnerId(user.getId()).stream()
        .map(purchasedResourceInUserMapper::toPurchasedResourceQuantityResponseDto)
        .toList();
  }

  private ResourceInUser getResourceInUser(UUID userId, UUID resourceId) {
    User user = userService.getUser(userId);
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

  public ResourceInUser getResourceInUser(User user, Resource resource) {
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

  public ResourceInUser removeQuantityFromResource(
      ResourceInUser resourceInUser, BigDecimal quantityToRemove) {

    BigDecimal totalQuantity = resourceInUser.getQuantity();
    BigDecimal newQuantity = totalQuantity.subtract(quantityToRemove);

    if (isNegative(newQuantity)) {
      throw new InsufficientResourceQuantityException(quantityToRemove, totalQuantity);
    }

    resourceInUser.setQuantity(newQuantity);
    User owner = resourceInUser.getOwner();
    if (isApproachingZero(newQuantity)) {
      owner.getResourcesOwned().remove(resourceInUser);
      resourceInUser = null;
    }
    userService.saveUser(owner);
    logger.debug("ResourceInUser after quantity removal: {}", resourceInUser);
    return resourceInUser;
  }

  private boolean isApproachingZero(BigDecimal value) {
    return value.abs().compareTo(EPSILON) < 0;
  }

  private boolean isNegative(BigDecimal newQuantity) {
    return newQuantity.compareTo(BigDecimal.ZERO) < 0;
  }

  public ResourceInUser findResourceInUserOrThrow(User previousOwner, UUID resourceId) {
    return findResourceInUser(previousOwner, resourceId)
        .orElseThrow(() -> new ResourceInUserNotFoundException(resourceId, previousOwner.getId()));
  }

  private Resource findResourceById(UUID resourceId) {
    logger.info("Finding resource by ID: {}", resourceId);
    return resourceService.getResourceById(resourceId);
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
    User user = userService.getUser(requestDto.getUserId());
    Resource resource = findResourceById(requestDto.getResourceId());
    ResourceInUser resourceInUser = addResourceToUser(user, resource, requestDto.getQuantity());
    resourceInUser.setDealPrice(requestDto.getDealPrice());

    ResourcesInUserResponseDto responseDto =
        resourcesInUserMapper.toResourcePurchaseResponse(resourceInUser);
    logger.info("Resource purchase completed successfully. Response: {}", responseDto);
    return responseDto;
  }
}
