package jewellery.inventory.integration;

import static jewellery.inventory.helper.UserTestHelper.createTestAdminUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRoleRequest;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.helper.SystemEventTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.model.Image;
import jewellery.inventory.model.OrganizationRole;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ImageService;
import jewellery.inventory.service.OrganizationRoleService;
import jewellery.inventory.service.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AuthenticatedIntegrationTestBase {

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
  @Autowired private ResourceInProductRepository resourceInProductRepository;
  @Autowired private PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  @Autowired private ResourceInOrganizationRepository resourceInOrganizationRepository;
  @Autowired private OrganizationRoleRepository organizationRoleRepository;
  @Autowired private OrganizationMembershipRepository organizationMembershipRepository;
  @Autowired private OrganizationRoleService organizationRoleService;

  @Autowired private ImageService imageService;
  @Autowired private ImageRepository imageRepository;

  protected HttpHeaders headers;
  protected User loggedInAdminUser;

  @BeforeEach
  void setup() {
    resourceInOrganizationRepository.deleteAll();
    deleteAllImages();
    saleRepository.deleteAll();
    productRepository.deleteAll();
    purchasedResourceInUserRepository.deleteAll();
    userRepository.deleteAll();
    resourceRepository.deleteAll();
    resourceInProductRepository.deleteAll();
    organizationRoleRepository.deleteAll();
    organizationMembershipRepository.deleteAll();
    loggedInAdminUser = createTestAdminUser();
    authenticateAs(loggedInAdminUser);
    setupTestRestTemplateWithAuthHeaders();
    loggedInAdminUser.setId(
        createUserInDatabase(UserTestHelper.getTestUserRequest(loggedInAdminUser)).getId());
    systemEventRepository.deleteAll();
  }

  protected String generateTokenForUser(User user) {
    try {
      return jwtService.generateToken(user);
    } catch (Exception e) {
      throw new RuntimeException("Error generating token for mock user", e);
    }
  }

  protected OrganizationRole createRoleWithAllPermissions() {
    Set<Permission> permissions = EnumSet.allOf(Permission.class);
    OrganizationRoleRequest organizationRoleRequest =
        new OrganizationRoleRequest("Admin", permissions);
    return organizationRoleService.createRole(organizationRoleRequest);
  }

  protected OrganizationRole createRole(String roleName, Set<Permission> permissions) {
    OrganizationRoleRequest organizationRoleRequest =
        new OrganizationRoleRequest(roleName, permissions);
    return organizationRoleService.createRole(organizationRoleRequest);
  }

  protected void createOrganizationMembership(UUID userId, UUID organizationId, UUID roleId) {
    organizationRoleService.assignRoleToUserInOrganization(userId, organizationId, roleId);
  }

  protected void authenticateAs(User user) {
    String mockToken = generateTokenForUser(user);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(user);
    headers = new HttpHeaders();
    headers.setBearerAuth(mockToken);
  }

  protected void setupTestRestTemplateWithAuthHeaders() {
    testRestTemplate
        .getRestTemplate()
        .setInterceptors(
            Collections.singletonList(
                (request, body, execution) -> {
                  request.getHeaders().addAll(headers);
                  return execution.execute(request, body);
                }));
  }

  protected User createAndPersistUser(UserRequestDto userRequestDto) {
    return createUserInDatabase(userRequestDto);
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

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequestDto) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity("/users", userRequestDto, User.class);
    return createUser.getBody();
  }
}
