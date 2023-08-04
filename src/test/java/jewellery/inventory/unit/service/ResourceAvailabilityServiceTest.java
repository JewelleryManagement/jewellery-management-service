package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.UserTestHelper.createResourceInUserRequestDto;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import jewellery.inventory.service.ResourceAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResourceAvailabilityServiceTest {
  @Mock private UserRepository userRepository;
  @InjectMocks private ResourceAvailabilityService resourceAvailabilityService;

  @Mock private ResourceRepository resourceRepository;
  private User user;
  private UserResponseDto userResponse;
  private UUID userId;
  private UUID resourceId;

  @BeforeEach
  void setUp() {
    user = createTestUser();
    userResponse = UserMapper.INSTANCE.toUserResponse(user);
    userId = UUID.randomUUID();
    resourceId = UUID.randomUUID();
  }

  @Test
  @DisplayName("Should add resource to user")
  void addResourceToUser() {
    ResourceInUserRequestDto resourceUserDto =
        createResourceInUserRequestDto(userId, resourceId, 10);

    User user = new User();
    user.setId(userId);

    Resource resource = new Resource();
    resource.setId(resourceId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

    resourceAvailabilityService.addResourceToUser(resourceUserDto);

    verify(userRepository, times(1)).findById(userId);
    verify(resourceRepository, times(1)).findById(resourceId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw an exception when user not found while adding resource")
  void addResourceToUserWhenUserNotFoundThenThrowException() {
    ResourceInUserRequestDto resourceUserDto =
        createResourceInUserRequestDto(userId, resourceId, 10);

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> resourceAvailabilityService.addResourceToUser(resourceUserDto));

    verify(userRepository, times(1)).findById(userId);
    verify(resourceRepository, never()).findById(resourceId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when resource does not exist")
  void addResourceToUserWhenResourceDoesNotExistThenThrowException() {
    ResourceInUserRequestDto resourceUserDto =
        createResourceInUserRequestDto(userId, resourceId, 10);

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User())); // changed this line
    when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> resourceAvailabilityService.addResourceToUser(resourceUserDto));

    verify(userRepository, times(1)).findById(userId);
    verify(resourceRepository, times(1)).findById(resourceId);
  }

  @Test
  @DisplayName("Should update the quantity of an existing resource in user")
  void addResourceToUserWhenResourceAlreadyInUser() {
    int initialResourceQuantity = 10;
    int addedResourceQuantity = 5;

    ResourceInUserRequestDto resourceUserDto =
        createResourceInUserRequestDto(userId, resourceId, addedResourceQuantity);

    User user = new User();
    user.setId(userId);

    Resource resource = new Resource();
    resource.setId(resourceId);

    ResourceInUser existingResourceInUser = new ResourceInUser();
    existingResourceInUser.setResource(resource);
    existingResourceInUser.setQuantity(initialResourceQuantity);
    user.addResource(existingResourceInUser);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

    resourceAvailabilityService.addResourceToUser(resourceUserDto);

    assertEquals(
        initialResourceQuantity + addedResourceQuantity, existingResourceInUser.getQuantity());
    verify(userRepository, times(1)).findById(userId);
    verify(resourceRepository, times(1)).findById(resourceId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should get all resources of a user")
  void getAllResourcesFromUser() {
    User user = createTestUser();
    user.setId(userId);

    ResourceInUser resourceInUser1 = new ResourceInUser();
    ResourceInUser resourceInUser2 = new ResourceInUser();
    user.setResourcesOwned(Arrays.asList(resourceInUser1, resourceInUser2));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    List<ResourceInUserResponseDto> resources =
        resourceAvailabilityService.getAllResourcesFromUser(userId);

    verify(userRepository, times(1)).findById(userId);

    assertEquals(2, resources.size());
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user not found")
  void getAllResourcesFromUserWhenUserNotFoundThenThrowException() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> resourceAvailabilityService.getAllResourcesFromUser(userId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should return empty list when user has no resources")
  void getAllResourcesFromUserWhenNoResources() {
    User user = createTestUser();
    user.setId(userId);
    user.setResourcesOwned(new ArrayList<>());
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    List<ResourceInUserResponseDto> resources =
        resourceAvailabilityService.getAllResourcesFromUser(userId);
    verify(userRepository, times(1)).findById(userId);

    assertTrue(resources.isEmpty());
  }

  @Test
  @DisplayName(
      "Should throw an exception when user does not exist while fetching resource quantity")
  void getUserResourceQuantityWhenUserNotFoundThenThrowException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> resourceAvailabilityService.getUserResourceQuantity(userId, resourceId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should throw an exception when user exists but does not have the resource")
  void getUserResourceQuantityWhenUserHasNoResourceThenThrowException() {
    User user = createTestUserWithId();
    user.setResourcesOwned(new ArrayList<>());

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        ResourceNotFoundException.class,
        () -> resourceAvailabilityService.getUserResourceQuantity(userId, resourceId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should return the quantity of the resource that the user has")
  void getUserResourceQuantityWhenUserHasResource() {
    User user = createTestUserWithId();

    ResourceInUser resourceInUser = new ResourceInUser();
    Resource resource = new Resource();
    resource.setId(resourceId);
    resourceInUser.setResource(resource);
    resourceInUser.setQuantity(10);
    resourceInUser.setOwner(user);

    user.setResourcesOwned(new ArrayList<>(List.of(resourceInUser)));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    double quantity = resourceAvailabilityService.getUserResourceQuantity(userId, resourceId);

    verify(userRepository, times(1)).findById(userId);

    assertEquals(10, quantity);
  }

  @Test
  @DisplayName(
      "Should throw an exception when user does not exist while removing resource quantity")
  void removeQuantityFromResourceWhenUserNotFoundThenThrowException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> resourceAvailabilityService.removeQuantityFromResource(userId, resourceId, 10));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should throw an exception when user exists but does not have the resource")
  void removeQuantityFromResourceWhenUserHasNoResourceThenThrowException() {
    User user = createTestUserWithId();
    user.setResourcesOwned(new ArrayList<>());

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        ResourceNotFoundException.class,
        () -> resourceAvailabilityService.removeQuantityFromResource(userId, resourceId, 10));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should throw an exception when negative quantity provided")
  void removeQuantityFromResourceWhenNegativeQuantityProvidedThenThrowException() {
    assertThrows(
        NegativeResourceQuantityException.class,
        () -> resourceAvailabilityService.removeQuantityFromResource(userId, resourceId, -10));
  }

  @Test
  @DisplayName("Should throw an exception when user does not have sufficient resource quantity")
  void removeQuantityFromResourceWhenInsufficientResourceQuantityThenThrowException() {
    User user = createTestUserWithId();

    ResourceInUser resourceInUser = new ResourceInUser();
    Resource resource = new Resource();
    resource.setId(resourceId);
    resourceInUser.setResource(resource);
    resourceInUser.setQuantity(5);
    resourceInUser.setOwner(user);

    user.setResourcesOwned(new ArrayList<>(List.of(resourceInUser)));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        InsufficientResourceQuantityException.class,
        () -> resourceAvailabilityService.removeQuantityFromResource(userId, resourceId, 10));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should reduce the resource quantity when user has sufficient resource quantity")
  void removeQuantityFromResourceWhenSufficientResourceQuantity() {
    User user = createTestUserWithId();

    ResourceInUser resourceInUser = new ResourceInUser();
    Resource resource = new Resource();
    resource.setId(resourceId);
    resourceInUser.setResource(resource);
    resourceInUser.setQuantity(20);
    resourceInUser.setOwner(user);

    user.setResourcesOwned(new ArrayList<>(List.of(resourceInUser)));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    resourceAvailabilityService.removeQuantityFromResource(userId, resourceId, 10);

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));

    assertEquals(10, resourceInUser.getQuantity());
  }

  @Test
  @DisplayName("Should remove resource from user when resource exists in user's resources")
  void removeResourceFromUserWhenResourceExists() {
    User user = createTestUserWithId();
    ResourceInUser resourceInUser = new ResourceInUser();
    Resource resource = new Resource();
    resource.setId(resourceId);
    resourceInUser.setResource(resource);
    user.setResourcesOwned(new ArrayList<>(List.of(resourceInUser)));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    resourceAvailabilityService.removeResourceFromUser(userId, resourceId);

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user does not exist")
  void removeResourceFromUserWhenUserDoesNotExistThenThrowException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class,
        () -> resourceAvailabilityService.removeResourceFromUser(userId, resourceId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName(
      "Should throw ResourceNotFoundException when user does not have the specified resource")
  void removeResourceFromUserWhenUserDoesNotHaveResourceThenThrowResourceNotFoundException() {
    User user = createTestUserWithId();
    UUID nonExistentResourceId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        ResourceNotFoundException.class,
        () -> resourceAvailabilityService.removeResourceFromUser(userId, nonExistentResourceId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }
}
