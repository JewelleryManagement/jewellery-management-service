package jewellery.inventory.unit.service.security;

import static jewellery.inventory.helper.UserTestHelper.USER_NAME;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import jewellery.inventory.exception.security.jwt.JwtExpiredException;
import jewellery.inventory.exception.security.jwt.JwtIsNotValidException;
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
class JwtTokenServiceTest {

  @InjectMocks private JwtTokenService jwtTokenService;
  @Mock private UserDetails userDetails;

  @Mock private JwtUtils jwtUtils;
  private final String SECRET_KEY = "QdzigVY4XWNItestqpRdNuCGXx+FXok5e++GeMm1OlE=";
  private static final String CLAIM_KEY = "claim1";
  private static final String CLAIM_VALUE = "value1";
  private static final long TOKEN_EXPIRATION = 3600000L;
  private Key key;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(jwtUtils, "secretKey", SECRET_KEY);
    ReflectionTestUtils.setField(jwtTokenService, "tokenExpiration", TOKEN_EXPIRATION);
    key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    when(jwtUtils.getSigningKey()).thenReturn(key);
    when(userDetails.getUsername()).thenReturn(USER_NAME);

    Claims claims = Jwts.claims().setSubject(USER_NAME);
    lenient().doReturn(claims).when(jwtUtils).extractAllClaims(anyString());
  }

  @Test
  void generateTokenWithValidInputsReturnsNotNullToken() {
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);
    assertThat(token).isNotNull();
  }

  @Test
  void generateTokenWithExtraClaimsReturnsCorrectClaims() {
    Map<String, Object> extraClaims = createExtraClaims();

    String token = jwtTokenService.generateToken(extraClaims, userDetails);

    Claims claims = parseClaimsFromToken(token);
    assertThat(claims).containsEntry(CLAIM_KEY, CLAIM_VALUE);
  }

  @Test
  void generateTokenWithValidInputsReturnsCorrectSubject() {
    Map<String, Object> extraClaims = new HashMap<>();

    String token = jwtTokenService.generateToken(extraClaims, userDetails);

    Claims claims = parseClaimsFromToken(token);
    assertThat(claims.getSubject()).isEqualTo(USER_NAME);
  }

  @Test
  void extractNameWithValidTokenReturnsCorrectName() {
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);

    String name = jwtTokenService.extractName(token);

    assertThat(name).isEqualTo(USER_NAME);
  }

  @Test
  void isTokenValidWithTokenHavingInvalidNameThrowsInvalidNameInJwtException() {
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);

    when(userDetails.getUsername()).thenReturn("invalid_name");

    assertThatThrownBy(() -> jwtTokenService.isTokenValid(token, userDetails))
        .isInstanceOf(JwtIsNotValidException.class);
  }

  @Test
  void isTokenValidWithExpiredTokenThrowsJwtExpiredException() {
    ReflectionTestUtils.setField(jwtTokenService, "tokenExpiration", -1L);
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);

    assertThatThrownBy(() -> jwtTokenService.isTokenValid(token, userDetails))
        .isInstanceOf(JwtExpiredException.class);
  }

  @Test
  void generateTokenWithNullUserDetailsThrowsIllegalArgumentException() {
    lenient().when(jwtUtils.getSigningKey()).thenReturn(key);
    lenient().when(userDetails.getUsername()).thenReturn(USER_NAME);

    assertThatThrownBy(() -> jwtTokenService.generateToken(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("UserDetails cannot be null");
  }

  private Map<String, Object> createExtraClaims() {
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put(CLAIM_KEY, CLAIM_VALUE);
    return extraClaims;
  }

  private Claims parseClaimsFromToken(String token) {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    Key key = Keys.hmacShaKeyFor(keyBytes);
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }
}
