package jewellery.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceInUserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceInUserResponseDto;
import jewellery.inventory.exception.duplicateException.DuplicateEmailException;
import jewellery.inventory.exception.duplicateException.DuplicateNameException;
import jewellery.inventory.exception.invalidResourceQuantityException.InsufficientResourceQuantityException;
import jewellery.inventory.exception.invalidResourceQuantityException.NegativeResourceQuantityException;
import jewellery.inventory.exception.notFoundException.ResourceNotFoundException;
import jewellery.inventory.exception.notFoundException.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInUser;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final ResourceRepository resourceRepository;
  private final ResourceInUserRepository resourceInUserRepository;

  public List<UserResponseDto> getAllUsers() {
    return UserMapper.INSTANCE.toUserResponseList(userRepository.findAll());
  }

  public UserResponseDto getUser(UUID id) {
    return UserMapper.INSTANCE.toUserResponse(
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
  }

  public UserResponseDto createUser(UserRequestDto user) {
    User userToCreate = UserMapper.INSTANCE.toUserEntity(user);
    validateUserEmailAndName(userToCreate);
    return UserMapper.INSTANCE.toUserResponse(userRepository.save(userToCreate));
  }

  public UserResponseDto updateUser(UserRequestDto userRequest, UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    User userToUpdate = UserMapper.INSTANCE.toUserEntity(userRequest);
    userToUpdate.setId(id);

    validateUserEmailAndName(userToUpdate);

    return UserMapper.INSTANCE.toUserResponse(userRepository.save(userToUpdate));
  }

  public void deleteUser(UUID id) {
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
    } else {
      throw new UserNotFoundException(id);
    }
  }

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
    user.getResourcesOwned().removeIf(r -> r.getResource().getId().equals(resourceId));
    userRepository.save(user);
  }

  private void validateUserEmailAndName(User user) {
    if (isNameUsedByOtherUser(user.getName(), user.getId())) {
      throw new DuplicateNameException(user.getName());
    }

    if (isEmailUsedByOtherUser(user.getEmail(), user.getId())) {
      throw new DuplicateEmailException(user.getEmail());
    }
  }

  private boolean isEmailUsedByOtherUser(String email, UUID id) {
    Optional<User> existingUser = userRepository.findByEmail(email);
    return existingUser.isPresent() && (id == null || !existingUser.get().getId().equals(id));
  }

  private boolean isNameUsedByOtherUser(String name, UUID id) {
    Optional<User> existingUser = userRepository.findByName(name);
    return existingUser.isPresent() && (id == null || !existingUser.get().getId().equals(id));
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
