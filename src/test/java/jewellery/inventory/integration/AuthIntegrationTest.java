package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jewellery.inventory.dto.request.AuthenticationRequestDto;
import jewellery.inventory.dto.response.UserAuthDetailsDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthIntegrationTest extends AuthenticatedIntegrationTestBase {
  @Autowired private UserRepository userRepository;
  @MockBean private PasswordEncoder passwordEncoder;
  private User testUser;
  private final AuthenticationRequestDto authRequest = new AuthenticationRequestDto();
  private final HttpHeaders headers = new HttpHeaders();

  @BeforeEach
  void setUp() {
    this.setup();
    createAndSaveTestUser();
  }

  @Test
  void generateTokenSuccessfully() {
    setupAuthRequestAndHeaders();
    HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authRequest, headers);

    ResponseEntity<UserAuthDetailsDto> response =
        testRestTemplate.exchange(
            getBaseAuthUrl(), HttpMethod.POST, requestEntity, UserAuthDetailsDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  void fetchUsersWithInvalidTokenWillReturnUnauthorized() {
    String invalidToken = "invalid_token";
    addTokenToHeaders(invalidToken);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<Void> response =
        testRestTemplate.exchange(getBaseUserUrl(), HttpMethod.GET, requestEntity, Void.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void loginWithNullEmailCredentialReturnsBadRequest() {
    setupAuthRequestAndHeaders();
    authRequest.setEmail(null);
    HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authRequest, null);
    ResponseEntity<String> response =
        testRestTemplate.exchange(getBaseAuthUrl(), HttpMethod.POST, requestEntity, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("email"));
    assertTrue(response.getBody().contains("Email must not be blank, empty or null"));
  }

  @Test
  void loginWithNullPasswordReturnsBadRequest() {
    setupAuthRequestAndHeaders();
    authRequest.setPassword(null);
    HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authRequest, null);
    ResponseEntity<String> response =
        testRestTemplate.exchange(getBaseAuthUrl(), HttpMethod.POST, requestEntity, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("password"));
    assertTrue(response.getBody().contains("Password must not be blank, empty or null"));
  }

  private String getBaseAuthUrl() {
    return "/login";
  }

  private String getBaseUserUrl() {
    return "/users";
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
    mockPasswordEncoder();
    testUser.setPassword(passwordEncoder.encode(testUser.getPassword()));
    userRepository.save(testUser);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(testUser);
  }
}
