package jewellery.inventory.unit.service.security;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.USER_PASSWORD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.UserAuthDetailsDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.security.InvalidCredentialsException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.service.security.JwtTokenService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  private static final String TEST_TOKEN = "token";
  @InjectMocks private AuthService authService;
  @Mock private UserRepository userRepository;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtTokenService jwtTokenService;
  @Mock private UserMapper userMapper;

  private AuthenticationRequestDto authRequest;
  private User user;

  @BeforeEach
  public void setUp() {
    authRequest = new AuthenticationRequestDto();
    authRequest.setEmail(USER_EMAIL);
    authRequest.setPassword(USER_PASSWORD);

    user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(USER_EMAIL);
    user.setPassword(USER_PASSWORD);
  }

  @Test
  void authenticateWithValidCredentialsReturnsToken() {
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
    when(jwtTokenService.generateToken(user)).thenReturn(TEST_TOKEN);

    UserAuthDetailsDto response = authService.login(authRequest);

    assertEquals(TEST_TOKEN, response.getToken());
  }

  @Test
  void authenticateWithInvalidCredentialsThrowsInvalidCredentialsException() {
    user.setPassword("wrongPassword");
    when(authenticationManager.authenticate(any())).thenThrow(new InvalidCredentialsException());
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));

    assertThrows(InvalidCredentialsException.class, () -> authService.login(authRequest));
  }

  @Test
  void authenticateWithNonExistentUserThrowsUserNotFoundException() {
    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> authService.login(authRequest));
  }

  @Test
  void verifyMethodsCalledWithExpectedParameters() {
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
    when(jwtTokenService.generateToken(user)).thenReturn(TEST_TOKEN);

    authService.login(authRequest);

    verify(authenticationManager)
        .authenticate(new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD));
    verify(userRepository).findByEmail(USER_EMAIL);
    verify(jwtTokenService).generateToken(user);
    verify(userMapper)
        .toUserResponse(user);
  }

  @Test
  void shouldReturnExpectedUserResponse() {
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
    when(jwtTokenService.generateToken(user)).thenReturn(TEST_TOKEN);

    UserResponseDto userResponseDto = new UserResponseDto();
    userResponseDto.setEmail(USER_EMAIL);

    when(userMapper.toUserResponse(user)).thenReturn(userResponseDto);

    UserAuthDetailsDto response = authService.login(authRequest);

    assertEquals(userResponseDto, response.getUser());
    assertEquals(TEST_TOKEN, response.getToken());
  }
}
