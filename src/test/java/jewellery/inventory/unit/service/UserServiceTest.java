package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.USER_NAME;
import static jewellery.inventory.helper.UserTestHelper.createSecondTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.duplicateException.DuplicateEmailException;
import jewellery.inventory.exception.duplicateException.DuplicateNameException;
import jewellery.inventory.exception.notFoundException.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
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
}
