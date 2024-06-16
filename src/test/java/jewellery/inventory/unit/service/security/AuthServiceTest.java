package jewellery.inventory.unit.service.security;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.USER_PASSWORD;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.UserAuthDetailsDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.NoAuthenticatedUserException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.security.InvalidCredentialsException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.service.security.AuthService;
import jewellery.inventory.service.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

    user = createTestUserWithId();
  }

  @Test
  void authenticateWithValidCredentialsReturnsToken() {
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
    when(jwtTokenService.generateToken(user)).thenReturn(TEST_TOKEN);

    UserResponseDto userResponseDto = new UserResponseDto();
    userResponseDto.setEmail(USER_EMAIL);

    when(userMapper.toUserResponse(user)).thenReturn(userResponseDto);

    UserAuthDetailsDto response = authService.login(authRequest);

    assertEquals(TEST_TOKEN, response.getToken());
    assertEquals(userResponseDto, response.getUser());
    verify(authenticationManager)
        .authenticate(new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD));
    verify(userRepository).findByEmail(USER_EMAIL);
    verify(jwtTokenService).generateToken(user);
    verify(userMapper).toUserResponse(user);
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
  void willGetUserResponseWhenAuthenticatedUser() {
    User user = new User();
    UserResponseDto expectedUserResponseDto = new UserResponseDto();
    Authentication authentication = Mockito.mock(Authentication.class);
    Mockito.when(authentication.getPrincipal()).thenReturn(user);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(userMapper.toUserResponse(user)).thenReturn(expectedUserResponseDto);

    UserResponseDto actualUserResponseDto = authService.getCurrentUser();

    assertEquals(expectedUserResponseDto, actualUserResponseDto);
  }

  @Test
  void willThrowWhenAuthenticatedUserHasNullPrincipal() {
    Authentication auth = new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(NoAuthenticatedUserException.class, () -> authService.getCurrentUser());
  }

  @Test
  void willThrowWhenNoAuthenticatedUserForUserResponse() {
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(securityContext);

    assertThrows(NoAuthenticatedUserException.class, () -> authService.getCurrentUser());
  }
}
