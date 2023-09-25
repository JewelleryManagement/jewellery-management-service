package jewellery.inventory.service;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.USER_PASSWORD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.auth.AuthenticationRequestDto;
import jewellery.inventory.dto.auth.AuthenticationResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
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
  public void authenticateWithValidCredentialsReturnsToken() {
    when(authenticationManager.authenticate(any()))
        .thenReturn(null); // assuming no exception means success
    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
    when(jwtTokenService.generateToken(user)).thenReturn(TEST_TOKEN);

    AuthenticationResponseDto response = authService.authenticate(authRequest);

    assertEquals(TEST_TOKEN, response.getToken());
  }

  @Test
  public void authenticateWithInvalidCredentialsThrowsBadCredentialsException() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Invalid credentials"));

    assertThrows(BadCredentialsException.class, () -> authService.authenticate(authRequest));
  }

  @Test
  public void authenticateWithNonExistentUserThrowsUserNotFoundException() {
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> authService.authenticate(authRequest));
  }
}
