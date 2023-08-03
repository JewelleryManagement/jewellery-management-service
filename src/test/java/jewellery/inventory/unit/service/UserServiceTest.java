package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.USER_NAME;
import static jewellery.inventory.helper.UserTestHelper.createResourceInUserRequestDto;
import static jewellery.inventory.helper.UserTestHelper.createSecondTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;

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
  @DisplayName("Should return all users")
  void getAllUsers() {
    List<User> users = Arrays.asList(user, new User(), new User());

    when(userRepository.findAll()).thenReturn(users);

    List<UserResponseDto> returnedUsers = userService.getAllUsers();

    assertEquals(users.size(), returnedUsers.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when the user id does not exist")
  void getUserWhenUserIdDoesNotExistThenThrowException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should return the user when the user id exists")
  void getUserWhenUserIdExists() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserResponseDto result = userService.getUser(userId);

    assertEquals(userResponse, result);
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should throw a DuplicateEmailException when the email is already taken")
  void createUserWhenEmailIsAlreadyTakenThenThrowDuplicateEmailException() {
    UserRequestDto userRequest = UserMapper.INSTANCE.toUserRequest(user);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    assertThrows(DuplicateEmailException.class, () -> userService.createUser(userRequest));

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw a DuplicateNameException when the name is already taken")
  void createUserWhenNameIsAlreadyTakenThenThrowDuplicateNameException() {
    UserRequestDto userRequest = UserMapper.INSTANCE.toUserRequest(user);

    when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

    assertThrows(DuplicateNameException.class, () -> userService.createUser(userRequest));

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should create a new user when the email and name are not taken")
  void createUserWhenEmailAndNameAreNotTaken() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
    when(userRepository.findByName(user.getName())).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponseDto createdUser = userService.createUser(UserMapper.INSTANCE.toUserRequest(user));

    assertEquals(userResponse, createdUser);
    verify(userRepository, times(1)).findByEmail(user.getEmail());
    verify(userRepository, times(1)).findByName(user.getName());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw an exception when the user id does not exist")
  void updateUserWhenUserIdDoesNotExistThenThrowException() {
    user.setId(userId);
    UserRequestDto userRequest = UserMapper.INSTANCE.toUserRequest(user);
    when(userRepository.existsById(userId)).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> userService.updateUser(userRequest, userId));

    verify(userRepository, times(1)).existsById(userId);
    verify(userRepository, never()).save(user);
  }

  @Test
  @DisplayName("Should update the user when the user id exists")
  void updateUserWhenUserIdExists() {
    user.setId(userId);

    when(userRepository.existsById(userId)).thenReturn(true);
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponseDto updatedUser =
        userService.updateUser(UserMapper.INSTANCE.toUserRequest(user), userId);

    assertEquals(UserMapper.INSTANCE.toUserResponse(user), updatedUser);
    verify(userRepository, times(1)).existsById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void shouldThrowDuplicateNameExceptionWhenUpdatingWithDuplicateName() {
    UUID existingUserId = UUID.randomUUID();
    User existingUser = createTestUser();
    existingUser.setId(existingUserId);

    UUID updatingUserId = UUID.randomUUID();
    User updatingUser = createSecondTestUser();
    updatingUser.setId(updatingUserId);

    updatingUser.setName(USER_NAME);
    updatingUser.setEmail("different@mail.com");

    UserRequestDto updatingUserRequest = UserMapper.INSTANCE.toUserRequest(updatingUser);

    when(userRepository.existsById(updatingUserId)).thenReturn(true);
    when(userRepository.findByName(USER_NAME)).thenReturn(Optional.of(existingUser));

    assertThrows(
        DuplicateNameException.class,
        () -> userService.updateUser(updatingUserRequest, updatingUserId));
  }

  @Test
  void shouldThrowWhenUpdatingWithDuplicateEmail() {
    UUID existingUserId = UUID.randomUUID();
    User existingUser = createTestUser();
    existingUser.setId(existingUserId);

    UUID updatingUserId = UUID.randomUUID();
    User updatingUser = createSecondTestUser();
    updatingUser.setId(updatingUserId);

    updatingUser.setName("not duplicate name");
    updatingUser.setEmail(USER_EMAIL);

    UserRequestDto updatingUserRequest = new UserRequestDto();
    updatingUserRequest.setName(updatingUser.getName());
    updatingUserRequest.setEmail(updatingUser.getEmail());

    when(userRepository.existsById(updatingUserId)).thenReturn(true);
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(existingUser));

    assertThrows(
        DuplicateEmailException.class,
        () -> userService.updateUser(updatingUserRequest, updatingUserId));
  }

  @Test
  @DisplayName("Should delete the user when the user id exists")
  void deleteUserWhenUserIdExists() {
    user.setId(userId);
    when(userRepository.existsById(userId)).thenReturn(true);

    assertDoesNotThrow(() -> userService.deleteUser(userId));

    verify(userRepository, times(1)).deleteById(userId);
  }

  @Test
  @DisplayName("Should throw an exception when the user id does not exist")
  void deleteUserWhenUserIdDoesNotExistThenThrowException() {
    when(userRepository.existsById(userId)).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

    verify(userRepository, times(1)).existsById(userId);
    verify(userRepository, never()).deleteById(userId);
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

    userService.addResourceToUser(resourceUserDto);

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

    assertThrows(UserNotFoundException.class, () -> userService.addResourceToUser(resourceUserDto));

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
        ResourceNotFoundException.class, () -> userService.addResourceToUser(resourceUserDto));

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

    userService.addResourceToUser(resourceUserDto);

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

    List<ResourceInUserResponseDto> resources = userService.getAllResourcesFromUser(userId);

    verify(userRepository, times(1)).findById(userId);

    assertEquals(2, resources.size());
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user not found")
  void getAllResourcesFromUserWhenUserNotFoundThenThrowException() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getAllResourcesFromUser(userId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should return empty list when user has no resources")
  void getAllResourcesFromUserWhenNoResources() {
    User user = createTestUser();
    user.setId(userId);
    user.setResourcesOwned(new ArrayList<>());
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    List<ResourceInUserResponseDto> resources = userService.getAllResourcesFromUser(userId);
    verify(userRepository, times(1)).findById(userId);

    assertTrue(resources.isEmpty());
  }

  @Test
  @DisplayName(
      "Should throw an exception when user does not exist while fetching resource quantity")
  void getUserResourceQuantityWhenUserNotFoundThenThrowException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class, () -> userService.getUserResourceQuantity(userId, resourceId));

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
        () -> userService.getUserResourceQuantity(userId, resourceId));

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

    double quantity = userService.getUserResourceQuantity(userId, resourceId);

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
        () -> userService.removeQuantityFromResource(userId, resourceId, 10));

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
        () -> userService.removeQuantityFromResource(userId, resourceId, 10));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should throw an exception when negative quantity provided")
  void removeQuantityFromResourceWhenNegativeQuantityProvidedThenThrowException() {
    assertThrows(
        NegativeResourceQuantityException.class,
        () -> userService.removeQuantityFromResource(userId, resourceId, -10));
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
        () -> userService.removeQuantityFromResource(userId, resourceId, 10));

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

    userService.removeQuantityFromResource(userId, resourceId, 10);

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

    userService.removeResourceFromUser(userId, resourceId);

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user does not exist")
  void removeResourceFromUserWhenUserDoesNotExistThenThrowException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class, () -> userService.removeResourceFromUser(userId, resourceId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when user does not have the specified resource")
  void removeResourceFromUserWhenUserDoesNotHaveResourceThenThrowResourceNotFoundException() {
    User user = createTestUserWithId();
    UUID nonExistentResourceId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(ResourceNotFoundException.class, () ->
        userService.removeResourceFromUser(userId, nonExistentResourceId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }
}
