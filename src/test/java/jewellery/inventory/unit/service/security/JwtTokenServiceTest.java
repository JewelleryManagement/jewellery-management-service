package jewellery.inventory.unit.service.security;

import static jewellery.inventory.helper.UserTestHelper.USER_NAME;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import jewellery.inventory.exception.security.jwt.JwtExpiredException;
import jewellery.inventory.exception.security.jwt.JwtIsNotValidException;
import jewellery.inventory.security.JwtUtils;
import jewellery.inventory.service.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {
  @InjectMocks private JwtTokenService jwtTokenService;
  @Mock private UserDetails userDetails;
  @Mock private JwtUtils jwtUtils;
  private final String SECRET_KEY = "QdzigVY4XWNItestqpRdNuCGXx+FXok5e++GeMm1OlE=";
  private static final long TOKEN_EXPIRATION = 36000200000L;
  private static final long ONE_DAY_IN_MILLISECONDS = 1000 * 60 * 60 * 24;

  private static final String TOKEN_MOCK =
      "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb290QGdtYWlsLmNvbSIsImlhdCI6MTY5NjI0OTQxOSwiZXhwIjoxNjk2MzM1ODE5fQ.z5ZNMMRkFzJ7qYdIy-lI9ii2hLjomgav0prE2_DKUkQ";
  private static final String INVALID_TOKEN =
      "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb290QGdtYWlsLmNvbSIsImlhdCI6MTY5NTk5NTI4MCwiZXhwIjoxNjk1OTk2NzIwfQ.WZopY5dSj0v3g28dorFHk7XhuH2R-e6k6zmZ_G5C9ow";
  private Key key;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(jwtUtils, "secretKey", SECRET_KEY);
    ReflectionTestUtils.setField(jwtTokenService, "tokenExpiration", TOKEN_EXPIRATION);
    key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    lenient().when(jwtUtils.getSigningKey()).thenReturn(key);
    lenient().when(userDetails.getUsername()).thenReturn(USER_NAME);
  }

  @Test
  void generateTokenWithValidInputsReturnsNotNullToken() {
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);
    assertThat(token).isNotNull();
  }

  @Test
  void isTokenValidWithValidTokenReturnsTrue() {
    setupDefaultExtractAllClaimsBehavior();
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);

    assertTrue(jwtTokenService.isTokenValid(token, userDetails));
  }

  @Test
  void isTokenValidWhenJwtExceptionThrownThrowsJwtIsNotValidException() {
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);
    when(jwtUtils.extractAllClaims(anyString())).thenThrow(JwtIsNotValidException.class);
    assertThrows(
        JwtIsNotValidException.class, () -> jwtTokenService.isTokenValid(token, userDetails));
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
    setupDefaultExtractAllClaimsBehavior();
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);

    String name = jwtTokenService.extractName(token);

    assertThat(name).isEqualTo(USER_NAME);
  }

  @Test
  void isTokenValidWithTokenHavingInvalidNameThrowsInvalidNameInJwtException() {
    setupDefaultExtractAllClaimsBehavior();
    String token = jwtTokenService.generateToken(new HashMap<>(), userDetails);

    when(userDetails.getUsername()).thenReturn("invalid_name");

    assertThatThrownBy(() -> jwtTokenService.isTokenValid(token, userDetails))
        .isInstanceOf(JwtIsNotValidException.class);
  }

  @Test
  void isTokenValidWithExpiredTokenThrowsJwtExpiredException() {
    Claims expiredClaims =
        Jwts.claims()
            .setSubject(USER_NAME)
            .setExpiration(Date.from(Instant.now().minusMillis(10200L)));
    doReturn(expiredClaims).when(jwtUtils).extractAllClaims(anyString());

    assertThatThrownBy(() -> jwtTokenService.isTokenValid(TOKEN_MOCK, userDetails))
        .isInstanceOf(JwtExpiredException.class);
  }

  @Test
  void generateTokenWithNullUserDetailsThrowsIllegalArgumentException() {
    lenient().when(jwtUtils.getSigningKey()).thenReturn(key);
    lenient().when(userDetails.getUsername()).thenReturn(USER_NAME);

    assertThatThrownBy(() -> jwtTokenService.generateToken(null))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("UserDetails cannot be null");
  }

  @Test
  void generateTokenAndValidateWithDifferentKeyThrowsJwtIsNotValidException() {
    setupDefaultExtractAllClaimsBehavior();
    when(userDetails.getUsername()).thenReturn("fakeUser");
    String token = jwtTokenService.generateToken(userDetails);

    ReflectionTestUtils.setField(jwtUtils, "secretKey", "anotherSecretKey");

    assertThatThrownBy(() -> jwtTokenService.isTokenValid(token, userDetails))
        .isInstanceOf(JwtIsNotValidException.class);
  }

  @Test
  void extractNameWithInvalidSignatureThrowsJwtIsNotValidException() {
    doThrow(new JwtIsNotValidException()).when(jwtUtils).extractAllClaims(INVALID_TOKEN);
    assertThatThrownBy(() -> jwtTokenService.extractName(INVALID_TOKEN))
        .isInstanceOf(JwtIsNotValidException.class);
  }

  @Test
  void isTokenValidWithNullTokenExpirationThrowsJwtExpiredException() {
    Claims expiredClaims = Jwts.claims().setSubject(USER_NAME).setExpiration(null);
    doReturn(expiredClaims).when(jwtUtils).extractAllClaims(anyString());

    assertThatThrownBy(() -> jwtTokenService.isTokenValid(TOKEN_MOCK, userDetails))
        .isInstanceOf(JwtExpiredException.class);
  }

  @Test
  void isTokenValidReturnsTrueWhenTokenHasValidExpiration() {
    String tokenWithValidExpiration = TOKEN_MOCK;
    Claims claimsWithValidExpiration = mock(Claims.class);

    when(claimsWithValidExpiration.getSubject()).thenReturn(USER_NAME);
    when(claimsWithValidExpiration.getExpiration())
        .thenReturn(new Date(System.currentTimeMillis() + 1000000));

    when(jwtUtils.extractAllClaims(tokenWithValidExpiration)).thenReturn(claimsWithValidExpiration);

    assertTrue(jwtTokenService.isTokenValid(tokenWithValidExpiration, userDetails));
  }

  @Test
  void isTokenValidThrowsJwtIsNotValidExceptionWhenTokenIsInvalid() {
    String token = INVALID_TOKEN;
    UserDetails userDetailsMock = mock(UserDetails.class);
    when(jwtUtils.extractAllClaims(token)).thenThrow(JwtIsNotValidException.class);

    assertThrows(
        JwtIsNotValidException.class, () -> jwtTokenService.isTokenValid(token, userDetailsMock));
  }

  @Test
  void extractUserEmailSuccessfullyExtractsValidEmail() {
    mockClaims();
    when(jwtTokenService.extractName(TOKEN_MOCK)).thenReturn(USER_NAME);

    String actualEmail = jwtTokenService.extractUserEmail(TOKEN_MOCK);

    assertEquals(USER_NAME, actualEmail);
  }

  @ParameterizedTest
  @MethodSource("invalidEmailProvider")
  void extractUserEmailThrowsExceptionWhenEmailIsInvalid(String invalidEmail) {
    mockClaims();
    when(jwtTokenService.extractName(TOKEN_MOCK)).thenReturn(invalidEmail);

    assertThrows(JwtIsNotValidException.class, () -> jwtTokenService.extractUserEmail(TOKEN_MOCK));
  }

  @Test
  void extractAllClaimsThrowsJwtExpiredExceptionOnExpiredJwtException() {
    String expiredToken = generateExpiredToken();

    when(jwtUtils.extractAllClaims(expiredToken)).thenCallRealMethod();
    assertThrows(JwtExpiredException.class, () -> jwtUtils.extractAllClaims(expiredToken));
  }

  @Test
  void extractAllClaimsThrowsJwtIsNotValidExceptionOnSignatureException() {
    when(jwtUtils.extractAllClaims(INVALID_TOKEN)).thenCallRealMethod();
    assertThrows(JwtIsNotValidException.class, () -> jwtUtils.extractAllClaims(INVALID_TOKEN));
  }

  private void mockClaims() {
    Claims claimsWithValidExpiration = mock(Claims.class);
    when(jwtUtils.extractAllClaims(TOKEN_MOCK)).thenReturn(claimsWithValidExpiration);
  }

  private Claims parseClaimsFromToken(String token) {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    Key key = Keys.hmacShaKeyFor(keyBytes);
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  private void setupDefaultExtractAllClaimsBehavior() {
    Claims claims = Jwts.claims().setSubject(USER_NAME);
    claims.setExpiration(Date.from(Instant.now().plusMillis(TOKEN_EXPIRATION)));

    lenient().doReturn(claims).when(jwtUtils).extractAllClaims(anyString());
  }

  private static Stream<String> invalidEmailProvider() {
    return Stream.of(null, "", " ");
  }

  private String generateExpiredToken() {
    Date expiredDate =
        new Date(System.currentTimeMillis() - ONE_DAY_IN_MILLISECONDS);

    return Jwts.builder()
        .setSubject("testUser")
        .setExpiration(expiredDate)
        .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }
}
