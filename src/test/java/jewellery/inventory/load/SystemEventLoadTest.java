package jewellery.inventory.load;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.OrganizationTestHelper.getTestUserInOrganizationRequest;
import static jewellery.inventory.helper.ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto;
import static jewellery.inventory.helper.ResourceTestHelper.getPearlRequestDto;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.helper.ProductTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.integration.AuthenticatedIntegrationTestBase;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.model.User;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Disabled
public class SystemEventLoadTest extends AuthenticatedIntegrationTestBase {
  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("1000000");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105");

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private String getOrganizationUsersUrl(UUID organizationId) {
    return "/organizations/" + organizationId + "/users";
  }

  private String getBaseResourceUrl() {
    return buildUrl("resources");
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("organizations", "resources-availability");
  }

  private String getBaseEventsUrl() {
    return "/system-events";
  }

  private OrganizationResponseDto organizationResponseDto;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;
  private ResourceResponseDto resourceResponse;
  List<UUID> resourceIds;
  private ProductRequestDto productRequestDto;

  @BeforeEach
  void setUp() {
    organizationResponseDto = createOrganizationInDatabase(getTestOrganizationRequest());
    User user = createUserInDatabase(UserTestHelper.createTestUserRequest());
    userInOrganizationRequestDto = getTestUserInOrganizationRequest(user.getId());
    addUserInOrganization(organizationResponseDto.getId(), userInOrganizationRequestDto);
    resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);
    productRequestDto =
        ProductTestHelper.getProductRequestDtoForOrganization(
            user, organizationResponseDto.getId(), resourceResponse.getId(), RESOURCE_QUANTITY);

    resourceIds = new ArrayList<>();
    createResources();

    long start = System.nanoTime();
    createProducts();
    long end = System.nanoTime();
    long totalMs = (end - start) / 1_000_000;

    System.out.println("Total time for 10000 products: " + totalMs + " ms");
  }

  @Test
  void test() {
    for (int i = 0; i < 10; i++) {

      long start = System.nanoTime();

      Random random = new Random();
      int randomIndex = random.nextInt(resourceIds.size());

      ResponseEntity<List<SystemEvent>> events =
          this.testRestTemplate.exchange(
              getBaseEventsUrl() + "/relevant/" + resourceIds.get(randomIndex),
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<SystemEvent>>() {});
      long end = System.nanoTime();
      long totalMs = (end - start) / 1_000_000;

      System.out.println(
          "Total time for " + events.getBody().size() + " events: " + totalMs + " ms");
    }
  }

  private OrganizationResponseDto createOrganizationInDatabase(
      OrganizationRequestDto organizationRequestDto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), organizationRequestDto, OrganizationResponseDto.class);
    return response.getBody();
  }

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequestDto) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity("/users", userRequestDto, User.class);
    return createUser.getBody();
  }

  @Nullable
  private void addUserInOrganization(UUID organizationID, UserInOrganizationRequestDto requestDto) {
    ResponseEntity<OrganizationSingleMemberResponseDto> addUserInOrganization =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationID),
            requestDto,
            OrganizationSingleMemberResponseDto.class);
    assertEquals(HttpStatus.CREATED, addUserInOrganization.getStatusCode());
  }

  private ResourceResponseDto sendCreateResourceRequest() {
    ResourceRequestDto resourceRequest = getPearlRequestDto();
    resourceRequest.setSku(UUID.randomUUID().toString());
    ResponseEntity<ResourceResponseDto> resourceResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, ResourceResponseDto.class);

    assertEquals(HttpStatus.CREATED, resourceResponseEntity.getStatusCode());

    ResourceResponseDto createdResource = resourceResponseEntity.getBody();
    assertNotNull(createdResource);
    assertNotNull(createdResource.getId());
    return createdResource;
  }

  private ResponseEntity<ResourcesInOrganizationResponseDto> sendResourceToOrganization(
      ResourceInOrganizationRequestDto resourceInOrganizationRequest) {
    return this.testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(),
        resourceInOrganizationRequest,
        ResourcesInOrganizationResponseDto.class);
  }

  @Nullable
  private ResponseEntity<ProductsInOrganizationResponseDto> createProduct(
      ProductRequestDto productRequestDto) {
    return this.testRestTemplate.postForEntity(
        "/products", productRequestDto, ProductsInOrganizationResponseDto.class);
  }

  private ProductRequestDto setOwnerAndResourceToProductRequest(
      ProductRequestDto productRequestDto,
      UUID organizationId,
      List<UUID> resourceIds,
      BigDecimal quantity) {
    productRequestDto.setOwnerId(organizationId);

    List<ResourceQuantityRequestDto> resourcesAndQuantities = new ArrayList<>();

    for (int i = 0; i < resourceIds.size(); i++) {
      ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
      resourceQuantityRequestDto.setResourceId(resourceIds.get(i));
      resourceQuantityRequestDto.setQuantity(quantity);
      resourcesAndQuantities.add(resourceQuantityRequestDto);
    }
    productRequestDto.setResourcesContent(resourcesAndQuantities);
    return productRequestDto;
  }

  private void createResources() {
    for (int i = 0; i < 50; i++) {
      UUID id = sendCreateResourceRequest().getId();
      ResourceInOrganizationRequestDto resourceInOrganizationRequest =
          createResourceInOrganizationRequestDto(
              organizationResponseDto.getId(), id, RESOURCE_QUANTITY, RESOURCE_PRICE);
      ResponseEntity<ResourcesInOrganizationResponseDto> resource =
          sendResourceToOrganization(resourceInOrganizationRequest);
      resourceIds.add(
          resource.getBody().getResourcesAndQuantities().getFirst().getResource().getId());
    }
  }

  private void createProducts() {
    for (int i = 0; i < 10000; i++) {
      ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
          createProduct(
              setOwnerAndResourceToProductRequest(
                  productRequestDto,
                  organizationResponseDto.getId(),
                  resourceIds,
                  getBigDecimal("10")));
    }
  }
}
