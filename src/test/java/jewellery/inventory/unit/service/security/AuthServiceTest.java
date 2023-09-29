package jewellery.inventory.unit.service.security;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.USER_PASSWORD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.AuthenticationResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.exception.security.InvalidCredentialsException;
import jewellery.inventory.exception.security.jwt.JwtAuthenticationBaseException;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.security.JwtTokenService;
import jewellery.inventory.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  private static final String TEST_TOKEN = "token";
  @InjectMocks private AuthService authService;

  @Mock private UserRepository userRepository;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtTokenService jwtTokenService;

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

    AuthenticationResponseDto response = authService.authenticate(authRequest);

    assertEquals(TEST_TOKEN, response.getToken());
  }

  @Test
  void authenticateWithInvalidCredentialsThrowsBadCredentialsException() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new InvalidCredentialsException());

    assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(authRequest));
  }

  @Test
  void authenticateWithNonExistentUserThrowsUserNotFoundException() {
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> authService.authenticate(authRequest));
  }

  @Test
  void authenticateWhenJwtAuthenticationBaseExceptionIsThrownThrowsInvalidCredentialsException() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new JwtAuthenticationBaseException("error"));
    assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(authRequest));
  }
}
