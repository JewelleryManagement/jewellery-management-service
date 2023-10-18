package jewellery.inventory.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.TransferResourceResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.invalid_resource_quantity.NegativeResourceQuantityException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.mapper.ResourcesInUserMapper;
import jewellery.inventory.mapper.UserMapper;
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
  private final UserMapper userMapper;
  private final ResourceMapper resourceMapper;
  private static final double EPSILON = 1e-10;

  @Transactional
  public TransferResourceResponseDto transferResources(TransferResourceRequestDto transferResourceRequestDto) {

    User owner = getOwner(transferResourceRequestDto);
    User recipient = getRecipient(transferResourceRequestDto);
    Resource resourceToTransfer = getResourceToTransfer(transferResourceRequestDto);

    changeOwnersResourceQuantity(transferResourceRequestDto, owner, resourceToTransfer);
    ResourceInUser resourceInReceiver =
        changeReceiversResourceQuantity(transferResourceRequestDto, recipient, resourceToTransfer);

    return TransferResourceResponseDto.builder()
        .previousOwner(userMapper.toUserResponse(owner))
        .newOwner(userMapper.toUserResponse(recipient))
        .transferredResource(ResourceQuantityResponseDto.builder()
                .resource(resourceMapper.toResourceResponse(resourceToTransfer))
                .quantity(resourceInReceiver.getQuantity())
                .build())
        .build();
  }

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

  private ResourceInUser changeReceiversResourceQuantity(
      TransferResourceRequestDto transferResourceRequestDto,
      User recipient,
      Resource resourceToTransfer) {
    ResourceInUser resourceInUserToReceive =
        resourceInUserRepository
            .findByResourceIdAndOwnerId(resourceToTransfer.getId(), recipient.getId())
            .orElse(null);

    if (recipient.getResourcesOwned() == null) {
      recipient.setResourcesOwned(new ArrayList<>());
    }

    if (resourceInUserToReceive == null) {
      ResourceInUser resourceInUser = new ResourceInUser();
      resourceInUser.setResource(resourceToTransfer);
      resourceInUser.setOwner(recipient);
      resourceInUser.setQuantity(transferResourceRequestDto.getQuantity());
      return resourceInUserRepository.save(resourceInUser);
    } else {
      resourceInUserToReceive.setQuantity(
          resourceInUserToReceive.getQuantity() + transferResourceRequestDto.getQuantity());
      return resourceInUserRepository.save(resourceInUserToReceive);
    }
  }

  private void changeOwnersResourceQuantity(
      TransferResourceRequestDto transferResourceRequestDto,
      User owner,
      Resource resourceToTransfer) {
    ResourceInUser resourceInUserToTransfer =
        resourceInUserRepository
            .findByResourceIdAndOwnerId(resourceToTransfer.getId(), owner.getId())
            .orElseThrow(
                () ->
                    new ResourceInUserNotFoundException(resourceToTransfer.getId(), owner.getId()));

    if (resourceInUserToTransfer.getQuantity() < transferResourceRequestDto.getQuantity()) {
      throw new InsufficientResourceQuantityException(
          transferResourceRequestDto.getQuantity(), resourceInUserToTransfer.getQuantity());
    }

    resourceInUserToTransfer.setQuantity(
        resourceInUserToTransfer.getQuantity() - transferResourceRequestDto.getQuantity());
    resourceInUserRepository.save(resourceInUserToTransfer);

    if (resourceInUserToTransfer.getQuantity() == 0) {
      resourceInUserRepository.delete(resourceInUserToTransfer);
    }
  }

  private Resource getResourceToTransfer(TransferResourceRequestDto transferResourceRequestDto) {
    return resourceRepository
        .findById(transferResourceRequestDto.getResourceId())
        .orElseThrow(
            () -> new ResourceNotFoundException(transferResourceRequestDto.getResourceId()));
  }

  private User getRecipient(TransferResourceRequestDto transferResourceRequestDto) {
    return userRepository
        .findById(transferResourceRequestDto.getRecipientId())
        .orElseThrow(() -> new UserNotFoundException(transferResourceRequestDto.getRecipientId()));
  }

  private User getOwner(TransferResourceRequestDto transferResourceRequestDto) {
    return userRepository
        .findById(transferResourceRequestDto.getOwnerId())
        .orElseThrow(() -> new UserNotFoundException(transferResourceRequestDto.getOwnerId()));
  }
}
