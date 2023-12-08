package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.createSecondTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.UserUpdateRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateEmailException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;

  private User user;
  private UserResponseDto userResponse;
  private UUID userId;

  @BeforeEach
  void setUp() {
    user = createTestUser();
    userResponse = userMapper.toUserResponse(user);
    userId = UUID.randomUUID();
  }

  @Test
  @DisplayName("Should return all users")
  void getAllUsers() {
    List<User> users = Arrays.asList(user, new User(), new User());

    when(userRepository.findAll()).thenReturn(users);
    when(userMapper.toUserResponseList(users))
        .thenReturn(
            Arrays.asList(new UserResponseDto(), new UserResponseDto(), new UserResponseDto()));

    List<UserResponseDto> returnedUsers = userService.getAllUsers();

    assertEquals(users.size(), returnedUsers.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when the user id does not exist")
  void getUserWhenUserIdDoesNotExistThenThrowException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUserResponse(userId));

    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should return the user when the user id exists")
  void getUserWhenUserIdExists() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserResponseDto result = userService.getUserResponse(userId);

    assertEquals(userResponse, result);
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  @DisplayName("Should throw a DuplicateEmailException when the email is already taken")
  void createUserWhenEmailIsAlreadyTakenThenThrowDuplicateEmailException() {
    UserRequestDto userRequest = userMapper.toUserRequest(user);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userMapper.toUserEntity(userRequest)).thenReturn(user);

    assertThrows(DuplicateEmailException.class, () -> userService.createUser(userRequest));

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Should create a new user when the email is not taken")
  void createUserWhenEmailIsNotTaken() {
    UserRequestDto userRequest = userMapper.toUserRequest(user);

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toUserEntity(userRequest)).thenReturn(user);
    when(userMapper.toUserResponse(user)).thenReturn(userResponse);
    when(passwordEncoder.encode(any()))
        .thenReturn("$2a$10$WIgDfys.uGK53Q3V13l8AOYCH7M1cVHulX8klIq0PLB/KweY/Onhi");

    UserResponseDto createdUser = userService.createUser(userMapper.toUserRequest(user));

    assertEquals(userResponse, createdUser);
    verify(userRepository, times(1)).findByEmail(user.getEmail());
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw an exception when the user id does not exist")
  void updateUserWhenUserIdDoesNotExistThenThrowException() {
    user.setId(userId);
    UserRequestDto userRequest = userMapper.toUserRequest(user);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.updateUser(userRequest, userId));

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(user);
  }

  @Test
  @DisplayName("Should update the user when the user id exists")
  void updateUserWhenUserIdExists() {
    user.setId(userId);
    String originalPassword = user.getPassword();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserUpdateRequestDto userRequestDto = new UserRequestDto();
    userRequestDto.setFirstName("changedFirstName");
    userRequestDto.setLastName("changedLastName");
    userRequestDto.setEmail("change@gmail.com");
    userRequestDto.setNote("changedNote");

    when(userMapper.toUserUpdateRequest(user)).thenReturn(userRequestDto);
    when(userMapper.toUserEntity(userRequestDto)).thenReturn(user);

    UserResponseDto updatedUser = userService.updateUser(userMapper.toUserUpdateRequest(user), userId);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertEquals(userMapper.toUserResponse(user), updatedUser);
    assertEquals(originalPassword, savedUser.getPassword(), "Password should remain unchanged after update");
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void shouldThrowWhenUpdatingWithDuplicateEmail() {
    UUID existingUserId = UUID.randomUUID();
    User existingUser = createTestUser();
    existingUser.setId(existingUserId);

    UUID updatingUserId = UUID.randomUUID();
    User updatingUser = createSecondTestUser();
    updatingUser.setId(updatingUserId);

    updatingUser.setFirstName("not duplicate name");
    updatingUser.setEmail(USER_EMAIL);

    UserUpdateRequestDto updatingUserRequest = new UserRequestDto();
    updatingUserRequest.setFirstName(updatingUser.getFirstName());
    updatingUserRequest.setEmail(updatingUser.getEmail());

    when(userRepository.findById(updatingUserId)).thenReturn(Optional.of(updatingUser));
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(existingUser));
    when(userMapper.toUserEntity(updatingUserRequest)).thenReturn(updatingUser);

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
