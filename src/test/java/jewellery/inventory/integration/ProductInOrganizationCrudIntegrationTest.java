package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.getPearlRequestDto;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.helper.*;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.PreciousStone;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProductInOrganizationCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("100");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105");

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("organizations", "resources-availability");
  }

  private String getBaseResourceUrl() {
    return buildUrl("resources");
  }

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseOrganizationUrl() {
    return buildUrl("organizations");
  }

  private String getOrganizationProductsUrl(String organizationId) {
    return buildUrl("organizations", organizationId, "products");
  }

  private String getOrganizationProductsWithIdUrl(String organizationId, String productId) {
    return buildUrl("organizations", organizationId, "products", productId);
  }

  private PreciousStone preciousStone;

  private ProductRequestDto productRequestDto;
  private OrganizationResponseDto organization;
  private User user;

  @BeforeEach
  void setUp() {
    user = createUserInDatabase(UserTestHelper.createTestUserRequest());
    organization = createOrganization();
    preciousStone = createPreciousStoneInDatabase();
    productRequestDto = ProductTestHelper.getBaseProductRequestDtoForOrganization(user);
  }

  @Test
  void getAllProductsFromOrganizationSuccessfully() {
    OrganizationResponseDto organizationResponseDto = createOrganization();

    ResponseEntity<ProductsInOrganizationResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationProductsUrl(organizationResponseDto.getId().toString()),
            HttpMethod.GET,
            null,
            ProductsInOrganizationResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void createProductInOrganizationSuccessfully() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);

    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);

    ResponseEntity<ProductsInOrganizationResponseDto> getAllProductsInOrgResponse =
        this.testRestTemplate.exchange(
            getOrganizationProductsUrl(organizationResponseDto.getId().toString()),
            HttpMethod.GET,
            null,
            ProductsInOrganizationResponseDto.class);

    assertEquals(getAllProductsInOrgResponse.getBody().getProducts().size(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organizationResponseDto.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    assertNotNull(productInOrganizationResponse.getBody());
    assertEquals(productInOrganizationResponse.getBody().getProducts().size(), 1);
    assertEquals(HttpStatus.CREATED, productInOrganizationResponse.getStatusCode());
  }

  @Test
  void deleteProductInOrganizationSuccessfully() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = createResourceResponse();

    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);

    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);

    ResponseEntity<ProductsInOrganizationResponseDto> getAllProductsInOrgResponse =
        this.testRestTemplate.exchange(
            getOrganizationProductsUrl(organizationResponseDto.getId().toString()),
            HttpMethod.GET,
            null,
            ProductsInOrganizationResponseDto.class);

    assertEquals(getAllProductsInOrgResponse.getBody().getProducts().size(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organizationResponseDto.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    assertEquals(productInOrganizationResponse.getBody().getProducts().size(), 1);

    ResponseEntity<Void> response =
        this.testRestTemplate.exchange(
            getOrganizationProductsWithIdUrl(
                organizationResponseDto.getId().toString(),
                productInOrganizationResponse.getBody().getProducts().get(0).getId().toString()),
            HttpMethod.DELETE,
            null,
            Void.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  private OrganizationResponseDto createOrganization() {
    OrganizationRequestDto organizationRequestDto =
        OrganizationTestHelper.getTestOrganizationRequest();
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationUrl(), organizationRequestDto, OrganizationResponseDto.class);

    OrganizationResponseDto organizationResponseDto = response.getBody();
    assertNotNull(organizationResponseDto);
    return organizationResponseDto;
  }

  private ResourceResponseDto createResourceResponse() {
    ResourceRequestDto resourceRequest = getPearlRequestDto();
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
        getBaseOrganizationUrl() + "/products",
        productRequestDto,
        ProductsInOrganizationResponseDto.class);
  }

  @Nullable
  private PreciousStone createPreciousStoneInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getPreciousStoneRequestDto();
    ResponseEntity<PreciousStone> createResource =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, PreciousStone.class);

    return createResource.getBody();
  }

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequestDto) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity("/users", userRequestDto, User.class);
    return createUser.getBody();
  }

  private ProductRequestDto setOwnerAndResourceToProductRequest(
      ProductRequestDto productRequestDto,
      UUID organizationId,
      UUID resourceId,
      BigDecimal quantity) {
    productRequestDto.setOwnerId(organizationId);
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    resourceQuantityRequestDto.setResourceId(resourceId);
    resourceQuantityRequestDto.setQuantity(quantity);
    productRequestDto.setResourcesContent(List.of(resourceQuantityRequestDto));
    return productRequestDto;
  }
}
