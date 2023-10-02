package jewellery.inventory.unit.service.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import jewellery.inventory.exception.security.InvalidSecretKeyException;
import jewellery.inventory.security.JwtTokenService;
import jewellery.inventory.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {
  @InjectMocks private JwtUtils jwtUtils;
  @Mock private JwtTokenService jwtTokenService;
  @Mock private UserDetails userDetails;

  private final String SECRET_KEY = "QdzigVY4XWNItestqpRdNuCGXx+FXok5e++GeMm1OlE=";
  private static final long TOKEN_EXPIRATION = 36000200000L;
  private Key key;

  @BeforeEach
  public void setUp() {
    jwtTokenService = mock(JwtTokenService.class);
    userDetails = mock(UserDetails.class);
    ReflectionTestUtils.setField(jwtUtils, "secretKey", SECRET_KEY);
    ReflectionTestUtils.setField(jwtTokenService, "tokenExpiration", TOKEN_EXPIRATION);
    key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
  }

  @Test
  void testWeakKeyException() {
    ReflectionTestUtils.setField(jwtUtils, "secretKey", "shortKey");

    assertThatThrownBy(() -> jwtUtils.getSigningKey())
        .isInstanceOf(InvalidSecretKeyException.class);
  }

  //  @Test
  //  void isTokenValidWithExpiredTokenThrowsJwtExpiredException() {
  //  //  when(jwtUtils.getSigningKey()).thenReturn(key);
  //    when(userDetails.getUsername()).thenReturn(USER_NAME);
  //    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);
  //    Claims expiredClaims =
  //        Jwts.claims()
  //            .setSubject(USER_NAME)
  //            .setExpiration(Date.from(Instant.now().minusMillis(10000L)));
  //
  //    //doReturn(expiredClaims).when(jwtUtils).extractAllClaims(anyString());
  //
  //    assertThatThrownBy(() -> jwtTokenService.isTokenValid(token, userDetails))
  //        .isInstanceOf(JwtExpiredException.class);
  //  }
}
