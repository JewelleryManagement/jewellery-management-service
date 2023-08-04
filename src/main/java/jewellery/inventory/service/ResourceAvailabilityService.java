package jewellery.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.resource.ResourceInUserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceInUserResponseDto;
import jewellery.inventory.exception.invalidResourceQuantityException.InsufficientResourceQuantityException;
import jewellery.inventory.exception.invalidResourceQuantityException.NegativeResourceQuantityException;
import jewellery.inventory.exception.notFoundException.ResourceNotFoundException;
import jewellery.inventory.exception.notFoundException.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInUser;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceAvailabilityService {
  private final UserRepository userRepository;
  private final ResourceRepository resourceRepository;

  @Transactional
  public UserResponseDto addResourceToUser(ResourceInUserRequestDto resourceUserDto) {
    User user = findUserById(resourceUserDto.getUserId());
    Resource resource = findResourceById(resourceUserDto.getResourceId());

    ResourceInUser resourceInUser =
        findResourceInUser(user, resourceUserDto.getResourceId())
            .orElseGet(() -> createAndAddNewResourceInUser(user, resource, 0));

    resourceInUser.setQuantity(resourceInUser.getQuantity() + resourceUserDto.getQuantity());

    return UserMapper.INSTANCE.toUserResponse(userRepository.save(user));
  }

  public double getUserResourceQuantity(UUID userId, UUID resourceId) {
    User user = findUserById(userId);
    ResourceInUser resourceInUser =
        findResourceInUser(user, resourceId)
            .orElseThrow(() -> new ResourceNotFoundException(resourceId));
    return resourceInUser.getQuantity();
  }

  public List<ResourceInUserResponseDto> getAllResourcesFromUser(UUID userId) {
    User user = findUserById(userId);
    return user.getResourcesOwned().stream()
        .map(UserMapper.INSTANCE::toResourceInUserResponseDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void removeQuantityFromResource(UUID userId, UUID resourceId, double quantity) {
    if (quantity < 0) {
      throw new NegativeResourceQuantityException(quantity);
    }
    User user = findUserById(userId);
    ResourceInUser resourceInUser =
        findResourceInUser(user, resourceId)
            .orElseThrow(() -> new ResourceNotFoundException(resourceId));

    double totalQuantity = resourceInUser.getQuantity();
    double newQuantity = totalQuantity - quantity;
    if (newQuantity < 0) {
      throw new InsufficientResourceQuantityException(quantity, totalQuantity);
    }

    resourceInUser.setQuantity(newQuantity);
    userRepository.save(user);
  }

  @Transactional
  public void removeResourceFromUser(UUID userId, UUID resourceId) {
    User user = findUserById(userId);

    ResourceInUser resourceToRemove =
        user.getResourcesOwned().stream()
            .filter(r -> r.getResource().getId().equals(resourceId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(resourceId));

    user.getResourcesOwned().remove(resourceToRemove);
    userRepository.save(user);
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
