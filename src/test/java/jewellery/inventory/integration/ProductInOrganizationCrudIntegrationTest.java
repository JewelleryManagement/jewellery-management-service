package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestUserInOrganizationRequest;
import static jewellery.inventory.helper.ResourceTestHelper.getPearlRequestDto;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.model.EventType.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.http.HttpEntity;
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

  private String getProductUrl(UUID id) {
    return "/products/" + id;
  }

  private String getOrganizationProductsUrl(String organizationId) {
    return buildUrl("organizations", organizationId, "products");
  }

  private String getOrganizationUsersUrl(UUID organizationId) {
    return "/organizations/" + organizationId + "/users";
  }

  private PreciousStone preciousStone;
  private ProductRequestDto productRequestDto;
  private OrganizationResponseDto organization;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;

  @BeforeEach
  void setUp() {
    User user = createUserInDatabase(UserTestHelper.createTestUserRequest());
    organization = createOrganization();
    userInOrganizationRequestDto = getTestUserInOrganizationRequest(user.getId());
    preciousStone = createPreciousStoneInDatabase();
    productRequestDto =
        ProductTestHelper.getProductRequestDtoForOrganization(
            user, organization.getId(), preciousStone.getId(), RESOURCE_QUANTITY);
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
  void createProductInOrganizationSuccessfully() throws JsonProcessingException {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    addUserInOrganization(organizationResponseDto.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);
    assertProductsInOrganizationSize(organizationResponseDto.getId().toString(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organizationResponseDto.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    assertNotNull(productInOrganizationResponse.getBody());
    assertEquals(1, productInOrganizationResponse.getBody().getProducts().size());
    assertEquals(HttpStatus.CREATED, productInOrganizationResponse.getStatusCode());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(productInOrganizationResponse.getBody(), objectMapper);
    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_PRODUCT_CREATE, expectedEventPayload);
    assertProductsInOrganizationSize(organizationResponseDto.getId().toString(), 1);
  }

  @Test
  void createProductInOrganizationThrowUserNotPartOfOrganization() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);
    assertProductsInOrganizationSize(organizationResponseDto.getId().toString(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organizationResponseDto.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    assertEquals(HttpStatus.CONFLICT, productInOrganizationResponse.getStatusCode());
  }

  @Test
  void updateProductInOrganizationSuccessfully() throws JsonProcessingException {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    addUserInOrganization(organizationResponseDto.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);
    assertResourcesInOrganizationSize(organizationResponseDto.getId().toString(), 1);
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organizationResponseDto.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));
    assertResourcesInOrganizationSize(organizationResponseDto.getId().toString(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> updatedProductInOrganizationResponse =
        updateProduct(
            productRequestDto,
            productInOrganizationResponse.getBody().getProducts().get(0).getId().toString());

    assertEquals(HttpStatus.OK, updatedProductInOrganizationResponse.getStatusCode());
    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(
            productInOrganizationResponse.getBody(),
            Objects.requireNonNull(updatedProductInOrganizationResponse.getBody()),
            objectMapper);
    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_PRODUCT_UPDATE, expectedEventPayload);
    assertProductsInOrganizationSize(organizationResponseDto.getId().toString(), 1);
    assertResourcesInOrganizationSize(organizationResponseDto.getId().toString(), 0);
  }

  @Test
  void updateProductInOrganizationThrowUserIsNotPartOfOrganization() {
    OrganizationResponseDto organizationResponseDto = createOrganization();
    addUserInOrganization(organizationResponseDto.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationResponseDto.getId(),
            resourceResponse.getId(),
            RESOURCE_QUANTITY,
            RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource2 =
        sendResourceToOrganization(resourceInOrganizationRequest);
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organizationResponseDto.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));
    productRequestDto.setAuthors(List.of());
    User newUser = createUserInDatabase(UserTestHelper.createDifferentUserRequest());
    productRequestDto.setAuthors(List.of(newUser.getId()));

    ResponseEntity<ProductsInOrganizationResponseDto> updatedProductInOrganizationResponse =
        updateProduct(
            productRequestDto,
            productInOrganizationResponse.getBody().getProducts().get(0).getId().toString());

    assertEquals(HttpStatus.CONFLICT, updatedProductInOrganizationResponse.getStatusCode());
  }

  @Test
  void deleteProductInOrganizationSuccessfully() throws JsonProcessingException {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), preciousStone.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), preciousStone.getId(), RESOURCE_QUANTITY));
    UUID productId = productInOrganizationResponse.getBody().getProducts().get(0).getId();

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            getBaseOrganizationUrl() + getProductUrl(productId),
            HttpMethod.DELETE,
            null,
            String.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(productInOrganizationResponse.getBody(), objectMapper);
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_PRODUCT_DISASSEMBLY, expectedEventPayload);
    assertProductsInOrganizationSize(
        productInOrganizationResponse.getBody().getOrganization().getId().toString(), 0);
  }

  @Test
  void transferProductSuccessfully() throws JsonProcessingException {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), preciousStone.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), preciousStone.getId(), RESOURCE_QUANTITY));
    UUID productId = productInOrganizationResponse.getBody().getProducts().get(0).getId();
    OrganizationResponseDto createSecondOrganization = createOrganization();
    UUID organizationId = createSecondOrganization.getId();

    ResponseEntity<ProductsInOrganizationResponseDto> transferResponse =
        this.testRestTemplate.exchange(
            "/organizations/products/" + productId + "/transfer/" + organizationId,
            HttpMethod.PUT,
            null,
            ProductsInOrganizationResponseDto.class);

    assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
    assertNotNull(transferResponse.getBody());
    assertEquals(organizationId, transferResponse.getBody().getOrganization().getId());
    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(
            productInOrganizationResponse.getBody(),
            Objects.requireNonNull(transferResponse.getBody()),
            objectMapper);
    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_PRODUCT_TRANSFER, expectedEventPayload);
  }

  private void assertProductsInOrganizationSize(String organizationId, int assertSize) {
    ResponseEntity<ProductsInOrganizationResponseDto> organizationProductsResponse =
        this.testRestTemplate.exchange(
            getOrganizationProductsUrl(organizationId),
            HttpMethod.GET,
            null,
            ProductsInOrganizationResponseDto.class);

    assertEquals(assertSize, organizationProductsResponse.getBody().getProducts().size());
  }
  private void assertResourcesInOrganizationSize(String organizationId, int assertSize) {
    ResponseEntity<ResourcesInOrganizationResponseDto> organizationResourcesResponse =
            this.testRestTemplate.exchange(
                    getBaseResourceAvailabilityUrl() + "/" + organizationId,
                    HttpMethod.GET,
                    null,
                    ResourcesInOrganizationResponseDto.class);

    assertEquals(assertSize, organizationResourcesResponse.getBody().getResourcesAndQuantities().size());
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

  private ResourceResponseDto sendCreateResourceRequest() {
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
  private ResponseEntity<ProductsInOrganizationResponseDto> updateProduct(
      ProductRequestDto productRequestDto, String productId) {

    HttpEntity<ProductRequestDto> requestEntity = new HttpEntity<>(productRequestDto, headers);

    return this.testRestTemplate.exchange(
        getBaseOrganizationUrl() + "/products/" + productId,
        HttpMethod.PUT,
        requestEntity,
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

  @Nullable
  private void addUserInOrganization(UUID organizationID, UserInOrganizationRequestDto requestDto) {
    ResponseEntity<OrganizationSingleMemberResponseDto> addUserInOrganization =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationID),
            requestDto,
            OrganizationSingleMemberResponseDto.class);
    assertEquals(HttpStatus.CREATED, addUserInOrganization.getStatusCode());
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
