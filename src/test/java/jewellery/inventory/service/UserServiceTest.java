package jewellery.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.UserRequest;
import jewellery.inventory.dto.UserResponse;
import jewellery.inventory.exception.DuplicateEmailException;
import jewellery.inventory.exception.DuplicateNameException;
import jewellery.inventory.exception.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
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
  private User user;
  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setName("John");
    user.setEmail("john@example.com");
    userResponse = UserMapper.INSTANCE.toUserResponse(user);
  }

  @Test
  @DisplayName("Should return all users")
  void getAllUsers() {
    List<User> users = Arrays.asList(user, new User(), new User());

    when(userRepository.findAll()).thenReturn(users);

    List<UserResponse> returnedUsers = userService.getAllUsers();

    assertEquals(users.size(), returnedUsers.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when the user id does not exist")
  void getUserWhenUserIdDoesNotExistThenThrowException() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should return the user when the user id exists")
  void getUserWhenUserIdExists() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserResponse result = userService.getUser(userId);

    assertEquals(userResponse, result);
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should throw a DuplicateEmailException when the email is already taken")
  void createUserWhenEmailIsAlreadyTakenThenThrowDuplicateEmailException() {
    UserRequest userRequest = UserMapper.INSTANCE.toUserRequest(user);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    assertThrows(DuplicateEmailException.class, () -> userService.createUser(userRequest));

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw a DuplicateNameException when the name is already taken")
  void createUserWhenNameIsAlreadyTakenThenThrowDuplicateNameException() {
    UserRequest userRequest = UserMapper.INSTANCE.toUserRequest(user);

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

    UserResponse createdUser = userService.createUser(UserMapper.INSTANCE.toUserRequest(user));

    assertEquals(userResponse, createdUser);
    verify(userRepository, times(1)).findByEmail(user.getEmail());
    verify(userRepository, times(1)).findByName(user.getName());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw an exception when the user id does not exist")
  void updateUserWhenUserIdDoesNotExistThenThrowException() {
    UUID userId = UUID.randomUUID();
    user.setId(userId);
    UserRequest userRequest = UserMapper.INSTANCE.toUserRequest(user);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUser(userRequest, userId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(user);
  }

  @Test
  @DisplayName("Should update the user when the user id exists")
  void updateUserWhenUserIdExists() {
    UUID userId = UUID.randomUUID();
    user.setId(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponse updatedUser =
        userService.updateUser(UserMapper.INSTANCE.toUserRequest(user), userId);

    assertEquals(UserMapper.INSTANCE.toUserResponse(user), updatedUser);
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void shouldThrowWhenUpdatingWithDuplicateName() {
    UUID existingUserId = UUID.randomUUID();
    User existingUser = new User();
    existingUser.setId(existingUserId);
    existingUser.setName("existing");
    existingUser.setEmail("existing@example.com");

    UUID updatingUserId = UUID.randomUUID();
    User updatingUser = new User();
    updatingUser.setId(updatingUserId);
    updatingUser.setName("existing"); // duplicate name
    updatingUser.setEmail("updating@example.com");

    UserRequest updatingUserRequest = UserMapper.INSTANCE.toUserRequest(updatingUser);

    when(userRepository.findById(updatingUserId)).thenReturn(Optional.of(updatingUser));
    when(userRepository.findByName("existing")).thenReturn(Optional.of(existingUser));

    assertThrows(
        DuplicateNameException.class,
        () -> userService.updateUser(updatingUserRequest, updatingUserId));
  }

  @Test
  void shouldThrowWhenUpdatingWithDuplicateEmail() {
    UUID existingUserId = UUID.randomUUID();
    User existingUser = new User();
    existingUser.setId(existingUserId);
    existingUser.setName("existing");
    existingUser.setEmail("existing@example.com");

    UUID updatingUserId = UUID.randomUUID();
    User updatingUser = new User();
    updatingUser.setId(updatingUserId);
    updatingUser.setName("updating");
    updatingUser.setEmail("existing@example.com");

    UserRequest updatingUserRequest = new UserRequest();
    updatingUserRequest.setName(updatingUser.getName());
    updatingUserRequest.setEmail(updatingUser.getEmail());

    when(userRepository.findById(updatingUserId)).thenReturn(Optional.of(updatingUser));
    when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

    assertThrows(
        DuplicateEmailException.class,
        () -> userService.updateUser(updatingUserRequest, updatingUserId));
  }

  @Test
  @DisplayName("Should delete the user when the user id exists")
  void deleteUserWhenUserIdExists() {
    UUID userId = UUID.randomUUID();
    when(userRepository.existsById(userId)).thenReturn(true);

    assertDoesNotThrow(() -> userService.deleteUser(userId));

    verify(userRepository, times(1)).deleteById(userId);
  }

  @Test
  @DisplayName("Should throw an exception when the user id does not exist")
  void deleteUserWhenUserIdDoesNotExistThenThrowException() {
    UUID userId = UUID.randomUUID();

    when(userRepository.existsById(userId)).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));

    verify(userRepository, times(1)).existsById(userId);
    verify(userRepository, never()).deleteById(userId);
  }
}
