package jewellery.inventory.service;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.invalid_resource_quantity.NegativeResourceQuantityException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.ResourcesInUserMapper;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceInUserService {
  private final UserRepository userRepository;
  private final ResourceRepository resourceRepository;
  private final ResourceInUserRepository resourceInUserRepository;
  private final ResourcesInUserMapper resourcesInUserMapper;
  private static final double EPSILON = 1e-10;

  @Transactional
  public ResourcesInUserResponseDto addResourceToUser(ResourceInUserRequestDto resourceUserDto) {
    User user = findUserById(resourceUserDto.getUserId());
    Resource resource = findResourceById(resourceUserDto.getResourceId());

    ResourceInUser resourceInUser =
        findResourceInUser(user, resourceUserDto.getResourceId())
            .orElseGet(() -> createAndAddNewResourceInUser(user, resource, 0));

    resourceInUser.setQuantity(resourceInUser.getQuantity() + resourceUserDto.getQuantity());
    resourceInUserRepository.save(resourceInUser);
    return resourcesInUserMapper.toResourcesInUserResponseDto(user);
  }

  public ResourcesInUserResponseDto getAllResourcesFromUser(UUID userId) {
    User user = findUserById(userId);
    return resourcesInUserMapper.toResourcesInUserResponseDto(user);
  }

  @Transactional
  public void removeQuantityFromResource(UUID userId, UUID resourceId, double quantity) {
    if (quantity < 0) {
      throw new NegativeResourceQuantityException(quantity);
    }
    User user = findUserById(userId);
    ResourceInUser resourceInUser =
        findResourceInUser(user, resourceId)
            .orElseThrow(() -> new ResourceInUserNotFoundException(resourceId, userId));

    double totalQuantity = resourceInUser.getQuantity();
    double newQuantity = totalQuantity - quantity;

    if (newQuantity < 0) {
      throw new InsufficientResourceQuantityException(quantity, totalQuantity);
    }

    resourceInUser.setQuantity(newQuantity);
    if (Math.abs(newQuantity) < EPSILON) {
      user.getResourcesOwned().remove(resourceInUser);
    }
    userRepository.save(user);
  }

  @Transactional
  public void removeResourceFromUser(UUID userId, UUID resourceId) {
    User user = findUserById(userId);

    ResourceInUser resourceToRemove =
        user.getResourcesOwned().stream()
            .filter(r -> r.getResource().getId().equals(resourceId))
            .findFirst()
            .orElseThrow(() -> new ResourceInUserNotFoundException(resourceId, userId));

    user.getResourcesOwned().remove(resourceToRemove);
    userRepository.save(user);
  }

  public ResourceOwnedByUsersResponseDto getUsersAndQuantities(UUID resourceId) {
    Resource resource = findResourceById(resourceId);
    return resourcesInUserMapper.toResourcesOwnedByUsersResponseDto(resource);
  }

  private User findUserById(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }

  private Resource findResourceById(UUID resourceId) {
    return resourceRepository
        .findById(resourceId)
        .orElseThrow(() -> new ResourceNotFoundException(resourceId));
  }

  private Optional<ResourceInUser> findResourceInUser(User user, UUID resourceId) {
    return user.getResourcesOwned().stream()
        .filter(r -> r.getResource().getId().equals(resourceId))
        .findFirst();
  }

  private ResourceInUser createAndAddNewResourceInUser(
      User user, Resource resource, double quantity) {
    ResourceInUser resourceInUser = new ResourceInUser();
    resourceInUser.setOwner(user);
    resourceInUser.setResource(resource);
    resourceInUser.setQuantity(quantity);
    user.getResourcesOwned().add(resourceInUser);
    return resourceInUser;
  }

}
