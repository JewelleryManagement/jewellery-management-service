package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Collections;
import jewellery.inventory.model.User;
import jewellery.inventory.security.SecurityConfig;
import jewellery.inventory.service.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class AuthenticatedIntegrationTestBase {

  protected static final String BASE_URL_PATH = "http://localhost:";

  @Autowired protected TestRestTemplate testRestTemplate;

  @Autowired private JwtTokenService jwtService;

  @MockBean protected UserDetailsService userDetailsService;

  @Value(value = "${local.server.port}")
  protected int port;

  protected HttpHeaders headers;

  @BeforeEach
  void setup() {
    User adminUser = createTestUser();
    setupMockSecurityContext(adminUser);
    setupTestRestTemplateWithAuthHeaders();
  }

  protected String generateTokenForUser(User user) {
    try {
      return jwtService.generateToken(user);
    } catch (Exception e) {
      throw new RuntimeException("Error generating token for mock user", e);
    }
  }

  private void setupMockSecurityContext(User user) {
    String mockToken = generateTokenForUser(user);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(user);
    headers = new HttpHeaders();
    headers.setBearerAuth(mockToken);
  }

  private void setupTestRestTemplateWithAuthHeaders() {
    testRestTemplate
        .getRestTemplate()
        .setInterceptors(
            Collections.singletonList(
                (request, body, execution) -> {
                  request.getHeaders().addAll(headers);
                  return execution.execute(request, body);
                }));
  }
}
