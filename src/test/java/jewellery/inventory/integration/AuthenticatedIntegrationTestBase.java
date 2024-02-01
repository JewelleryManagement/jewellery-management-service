package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.createTestUserWithId;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import jewellery.inventory.helper.SystemEventTestHelper;
import jewellery.inventory.model.Image;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ImageService;
import jewellery.inventory.service.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class AuthenticatedIntegrationTestBase {

  @Autowired protected ObjectMapper objectMapper;

  @Autowired protected TestRestTemplate testRestTemplate;
  @Autowired protected SystemEventTestHelper systemEventTestHelper;
  @Autowired private JwtTokenService jwtService;
  @MockBean protected UserDetailsService userDetailsService;
  @Autowired private UserRepository userRepository;
  @Autowired private SystemEventRepository systemEventRepository;
  @Autowired private SaleRepository saleRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private ResourceInUserRepository resourceInUserRepository;
  @Autowired private ResourceInProductRepository resourceInProductRepository;
  @Autowired private PurchasedResourceInUserRepository purchasedResourceInUserRepository;

  @Autowired private ImageService imageService;
  @Autowired private ImageRepository imageRepository;
  @Autowired private ProductPriceDiscountRepository productPriceDiscountRepository;

  protected HttpHeaders headers;

  @BeforeEach
  void setup() {
    deleteAllImages();
    productPriceDiscountRepository.deleteAll();
    productRepository.deleteAll();
    purchasedResourceInUserRepository.deleteAll();
    saleRepository.deleteAll();
    userRepository.deleteAll();
    resourceRepository.deleteAll();
    resourceInUserRepository.deleteAll();
    resourceInProductRepository.deleteAll();
    systemEventRepository.deleteAll();
    User adminUser = createTestUserWithId();
    setupMockSecurityContext(adminUser);
    setupTestRestTemplateWithAuthHeaders();
  }

  private void deleteAllImages() {
    imageRepository.findAll().forEach(this::deleteImage);
  }

  private void deleteImage(Image image) {
    try {
      imageService.deleteImage(image.getProduct().getId());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
