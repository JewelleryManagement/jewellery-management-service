package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.USER_EMAIL;
import static jewellery.inventory.helper.UserTestHelper.USER_NAME;
import static jewellery.inventory.helper.UserTestHelper.USER_PASSWORD;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.AuthenticationResponseDto;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthIntegrationTest extends AuthenticatedIntegrationTestBase {

  @Autowired private UserRepository userRepository;
  @MockBean private PasswordEncoder passwordEncoder;

  private User testUser;

  private final AuthenticationRequestDto authRequest = new AuthenticationRequestDto();
  private final HttpHeaders headers = new HttpHeaders();

  @Override
  @BeforeEach
  void setup() {
    userRepository.deleteAll();
    SecurityContextHolder.clearContext();
    mockPasswordEncoder();
    createAndSaveTestUser();
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUser);
    setupAuthRequestAndHeaders();
  }

  @Test
  void generateTokenSuccessfully() {
    HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authRequest, headers);

    ResponseEntity<AuthenticationResponseDto> response =
        testRestTemplate.exchange(
            getBaseAuthUrl() + "/token",
            HttpMethod.POST,
            requestEntity,
            AuthenticationResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  void testUnauthorizedAccessWithInvalidToken() {
    String invalidToken = "invalid_token";
    addTokenToHeaders(invalidToken);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<Void> response =
        testRestTemplate.exchange(getBaseUserUrl(), HttpMethod.GET, requestEntity, Void.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testAccessToProtectedEndpointWithValidToken() {
    String mockToken = generateTokenForUser(testUser);
    addTokenToHeaders(mockToken);
    HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authRequest, headers);

    ResponseEntity<String> response =
        testRestTemplate.exchange(getBaseUserUrl(), HttpMethod.GET, requestEntity, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    String responseBody = response.getBody();
    assertNotNull(responseBody);
    assertTrue(responseBody.contains(USER_NAME));
    assertTrue(responseBody.contains(USER_EMAIL));
  }

  @Test
  void testInvalidUrlReturnsUnauthorized() {
    ResponseEntity<Void> response =
        testRestTemplate.exchange("/invalid-url", HttpMethod.GET, null, Void.class);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  private String getBaseAuthUrl() {
    return BASE_URL_PATH + port + "/auth";
  }

  private String getBaseUserUrl() {
    return BASE_URL_PATH + port + "/users";
  }

  private void setupAuthRequestAndHeaders() {
    authRequest.setEmail(USER_EMAIL);
    authRequest.setPassword(USER_PASSWORD);
    headers.setContentType(MediaType.APPLICATION_JSON);
  }

  private void addTokenToHeaders(String token) {
    headers.setBearerAuth(token);
  }

  private void mockPasswordEncoder() {
    when(passwordEncoder.encode(USER_PASSWORD)).thenReturn("1234Encoded");
    when(passwordEncoder.matches(USER_PASSWORD, "1234Encoded")).thenReturn(true);
  }

  private void createAndSaveTestUser() {
    testUser = createTestUser();
    testUser.setPassword(passwordEncoder.encode(testUser.getPassword()));
    userRepository.save(testUser);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUser);
  }
}
