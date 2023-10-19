package jewellery.inventory.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.UUID;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

  @InjectMocks private JwtUtils jwtUtils;

  @Mock private User user;

  @BeforeEach
  public void setUp() {
    jwtUtils = new JwtUtils();
  }

  @Test
  public void testGetCurrentUserIdWhenAuthenticatedUserThenReturnUserId() {
    UUID userId = UUID.randomUUID();
    when(user.getId()).thenReturn(userId);

    Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UUID result = jwtUtils.getCurrentUserId();
    assertEquals(userId, result);
  }

  @Test
  public void testGetCurrentUserIdWhenNoAuthenticatedUserThenThrowException() {
    SecurityContextHolder.getContext().setAuthentication(null);

    assertThrows(RuntimeException.class, () -> jwtUtils.getCurrentUserId());
  }

  @Test
  public void testGetCurrentUserIdWhenInvalidPrincipalTypeThenThrowException() {
    Authentication auth = new UsernamePasswordAuthenticationToken("invalid", null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(RuntimeException.class, () -> jwtUtils.getCurrentUserId());
  }
}
