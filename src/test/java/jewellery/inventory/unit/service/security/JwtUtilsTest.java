package jewellery.inventory.unit.service.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.UUID;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.security.InvalidSecretKeyException;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.User;
import jewellery.inventory.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

  @InjectMocks private JwtUtils jwtUtils;

  @Mock private User user;
  @Mock private UserMapper userMapper;

  private static final String SECRET_KEY = "3upwHfxELiEU8C7+DEOLJxQWVqKQ/abSpdzixM8YQ+I=";

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(jwtUtils, "secretKey", SECRET_KEY);
    SecurityContextHolder.clearContext();
  }

  @Test
  public void willGetUserIdWhenAuthenticatedUser() {
    UUID userId = UUID.randomUUID();
    when(user.getId()).thenReturn(userId);

    Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UUID result = jwtUtils.getCurrentUserId();
    assertEquals(userId, result);
  }

  @Test
  public void willThrowWhenNoAuthenticatedUser() {
    SecurityContextHolder.getContext().setAuthentication(null);

    assertThrows(RuntimeException.class, () -> jwtUtils.getCurrentUserId());
  }

  @Test
  public void willThrowWhenInvalidPrincipalType() {
    Authentication auth = new UsernamePasswordAuthenticationToken("invalid", null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(RuntimeException.class, () -> jwtUtils.getCurrentUserId());
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

    UserResponseDto actualUserResponseDto = jwtUtils.getCurrentUser();

    assertEquals(expectedUserResponseDto, actualUserResponseDto);
  }

  @Test
  void willThrowWhenNoAuthenticatedUserForUserResponse() {
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(securityContext);

    assertThrows(RuntimeException.class, () -> jwtUtils.getCurrentUser());
  }

  @Test
  public void willThrowWhenWeakKey() {
    String weakKey = "weakKey";
    ReflectionTestUtils.setField(jwtUtils, "secretKey", weakKey);

    assertThrows(InvalidSecretKeyException.class, () -> jwtUtils.getSigningKey());
  }
}
