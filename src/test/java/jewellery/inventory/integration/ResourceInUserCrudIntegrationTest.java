package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto;
import static jewellery.inventory.helper.SaleTestHelper.getSimpleSaleInOrganizationRequestDto;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.DiamondResponseDto;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

class ResourceInUserCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("5");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105.5");
  private static final BigDecimal SALE_DISCOUNT = getBigDecimal("10");

  private User buyer;
  private DiamondResponseDto diamond;

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseOrganizationsUrl() {
    return buildUrl("organizations");
  }

  private String getBaseResourceInOrganizationAvailabilityUrl() {
    return buildUrl("organizations", "resources-availability");
  }

  private String getBaseOrganizationSaleUrl() {
    return buildUrl("organizations", "sales");
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("resources", "availability", "purchased");
  }

  private String getBaseUserUrl() {
    return buildUrl("users");
  }

  private String getBaseResourceUrl() {
    return buildUrl("resources");
  }

  @BeforeEach
  void setUp() {
    buyer = createUserInDatabase(createDifferentUserRequest());
    diamond = createDiamondInDatabase();
  }

  @Test
  void getAllPurchasedResourcesFromUserSuccessfully() {
    createSaleInOrganization();

    ResponseEntity<List<PurchasedResourceQuantityResponseDto>> response =
        getPurchasedResources(buyer.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals(
        diamond.getId(),
        response.getBody().getFirst().getResourceAndQuantity().getResource().getId());
  }

  @Test
  void getAllPurchasedResourcesFromUserReturnsEmptyWhenUserHasNoResources() {
    ResponseEntity<List<PurchasedResourceQuantityResponseDto>> response =
        getPurchasedResources(buyer.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
  }

  private Organization createOrganizationInDatabase(
      OrganizationRequestDto testOrganizationRequest) {
    ResponseEntity<Organization> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), testOrganizationRequest, Organization.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    return response.getBody();
  }

  private User createUserInDatabase(UserRequestDto userRequest) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, User.class);

    return createUser.getBody();
  }

  private DiamondResponseDto createDiamondInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getDiamondRequestDto();
    ResponseEntity<DiamondResponseDto> createResource =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, DiamondResponseDto.class);

    return createResource.getBody();
  }

  private ResourcesInOrganizationResponseDto createResourceInOrganization(
      ResourceInOrganizationRequestDto request) {
    ResponseEntity<ResourcesInOrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseResourceInOrganizationAvailabilityUrl(),
            request,
            ResourcesInOrganizationResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    return response.getBody();
  }

  private void createSaleInOrganization() {
    Organization organizationSeller =
        createOrganizationInDatabase(OrganizationTestHelper.getTestOrganizationRequest());

    ResourceInOrganizationRequestDto resourceInOrganizationRequestDto =
        createResourceInOrganizationRequestDto(
            organizationSeller.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);

    ResourcesInOrganizationResponseDto resourcesInOrganizationResponseDto =
        createResourceInOrganization(resourceInOrganizationRequestDto);

    SaleRequestDto saleRequestDto =
        getSimpleSaleInOrganizationRequestDto(
            organizationSeller.getId(),
            buyer.getId(),
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);

    this.testRestTemplate.postForEntity(
        getBaseOrganizationSaleUrl(), saleRequestDto, OrganizationSaleResponseDto.class);
  }

  private ResponseEntity<List<PurchasedResourceQuantityResponseDto>> getPurchasedResources(
      UUID buyerId) {

    return testRestTemplate.exchange(
        getBaseResourceAvailabilityUrl() + "/" + buyerId,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<PurchasedResourceQuantityResponseDto>>() {});
  }
}
