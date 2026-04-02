package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestUserInOrganizationRequest;
import static jewellery.inventory.helper.ResourceTestHelper.getPearlRequestDto;
import static jewellery.inventory.helper.SaleTestHelper.getSaleInOrganizationRequestDto;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static jewellery.inventory.model.EventType.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.util.*;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.helper.*;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Diamond;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProductInOrganizationCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("100");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105");
  private static final BigDecimal SALE_DISCOUNT = getBigDecimal("10");
  private static final BigDecimal RESOURCE_SALE_QUANTITY = getBigDecimal("10");

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

  private String getProductsUrlByResource(String resourceId) {
    return buildUrl("products", "resource", resourceId);
  }

  private String getOrganizationUsersUrl(UUID organizationId) {
    return "/organizations/" + organizationId + "/users";
  }

  private String getBaseSaleUrl() {
    return "/sales";
  }

  private String getProductByOwnerUrl(UUID ownerId) {
    return buildUrl("products", "/by-owner/" + ownerId);
  }

  private Diamond diamond;
  private ProductRequestDto productRequestDto;
  private OrganizationResponseDto organization;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;
  private RoleResponseDto roleWithAllPermissions;

  @BeforeEach
  void setUp() {
    User user = createUserInDatabase(UserTestHelper.createTestUserRequest());
    organization = createOrganization();
    userInOrganizationRequestDto = getTestUserInOrganizationRequest(user.getId());
    diamond = createDiamondInDatabase();
    productRequestDto =
        ProductTestHelper.getProductRequestDtoForOrganization(
            user, organization.getId(), diamond.getId(), RESOURCE_QUANTITY);
  }

  @Test
  void getAllProductsFromOrganizationSuccessfully() {
    ResponseEntity<ProductsInOrganizationResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationProductsUrl(organization.getId().toString()),
            HttpMethod.GET,
            null,
            ProductsInOrganizationResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void createProductInOrganizationSuccessfully() throws JsonProcessingException {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    sendResourceToOrganization(resourceInOrganizationRequest);
    assertProductsInOrganizationSize(organization.getId().toString(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    assertNotNull(productInOrganizationResponse.getBody());
    assertEquals(1, productInOrganizationResponse.getBody().getProducts().size());
    assertEquals(HttpStatus.CREATED, productInOrganizationResponse.getStatusCode());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(productInOrganizationResponse.getBody(), objectMapper);
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_PRODUCT_CREATE,
        expectedEventPayload,
        productInOrganizationResponse.getBody().getProducts().getFirst().getId());
    assertProductsInOrganizationSize(organization.getId().toString(), 1);
  }

  @Test
  void createProductInOrganizationThrowUserNotPartOfOrganization() {
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);
    assertProductsInOrganizationSize(organization.getId().toString(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    assertEquals(HttpStatus.CONFLICT, productInOrganizationResponse.getStatusCode());
  }

  @Test
  void updateProductInOrganizationSuccessfully() throws JsonProcessingException {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    sendResourceToOrganization(resourceInOrganizationRequest);
    assertResourcesInOrganizationSize(organization.getId().toString(), 1);
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));
    assertResourcesInOrganizationSize(organization.getId().toString(), 0);

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
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_PRODUCT_UPDATE,
        expectedEventPayload,
        updatedProductInOrganizationResponse.getBody().getProducts().getFirst().getId());
    assertProductsInOrganizationSize(organization.getId().toString(), 1);
    assertResourcesInOrganizationSize(organization.getId().toString(), 0);
  }

  @Test
  void updateProductInOrganizationThrowUserIsNotPartOfOrganization() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource2 =
        sendResourceToOrganization(resourceInOrganizationRequest);
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
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
            organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_QUANTITY));
    UUID productId = productInOrganizationResponse.getBody().getProducts().get(0).getId();

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            getProductUrl(productId), HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(productInOrganizationResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_PRODUCT_DISASSEMBLY, expectedEventPayload, productId);
    assertProductsInOrganizationSize(
        productInOrganizationResponse.getBody().getOrganization().getId().toString(), 0);
  }

  @Test
  void transferProductSuccessfully() throws JsonProcessingException {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_QUANTITY));
    UUID productId = productInOrganizationResponse.getBody().getProducts().get(0).getId();
    OrganizationResponseDto createSecondOrganization = createOrganization();
    UUID organizationId = createSecondOrganization.getId();

    ResponseEntity<ProductsInOrganizationResponseDto> transferResponse =
        this.testRestTemplate.exchange(
            "/products/" + productId + "/transfer/" + organizationId,
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
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_PRODUCT_TRANSFER,
        expectedEventPayload,
        transferResponse.getBody().getProducts().getFirst().getId());
  }

  @Test
  void getAllProductsByResourceReturnsEmptyArrayWhenResourceIsNotPartOfProduct() {
    ResponseEntity<List<ProductResponseDto>> response =
        testRestTemplate.exchange(
            getProductsUrlByResource(diamond.getId().toString()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    List<ProductResponseDto> products = response.getBody();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, products.size());
  }

  @Test
  void getAllProductsByResourceSuccessfully() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    ResponseEntity<ResourcesInOrganizationResponseDto> resource =
        sendResourceToOrganization(resourceInOrganizationRequest);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    ResponseEntity<List<ProductResponseDto>> response =
        testRestTemplate.exchange(
            getProductsUrlByResource(
                resource
                    .getBody()
                    .getResourcesAndQuantities()
                    .get(0)
                    .getResource()
                    .getId()
                    .toString()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    List<ProductResponseDto> products = response.getBody();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, products.size());
    assertEquals(
        productInOrganizationResponse.getBody().getProducts().get(0).getId(),
        products.get(0).getId());
  }

  @Test
  void getAllProductsInOrganizationShouldThrowWhenUserHasNoProductReadPermission() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getOrganizationProductsUrl(String.valueOf(organization.getId())), String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getProductShouldThrowWhenUserHasNoProductReadPermission() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    sendResourceToOrganization(resourceInOrganizationRequest);
    assertProductsInOrganizationSize(organization.getId().toString(), 0);

    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));

    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getProductUrl(productInOrganizationResponse.getBody().getProducts().getFirst().getId()),
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void createProductShouldThrowWhenUserHasNoProductCreatePermission() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    sendResourceToOrganization(resourceInOrganizationRequest);
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.postForEntity("/products", productRequestDto, String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void deleteProductShouldThrowWhenUserHasNoProductDeletePermission() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    sendResourceToOrganization(resourceInOrganizationRequest);
    ResponseEntity<ProductsInOrganizationResponseDto> product =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            getProductUrl(product.getBody().getProducts().getFirst().getId()),
            HttpMethod.DELETE,
            null,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void updateProductShouldThrowWhenUserHasNoProductUpdatePermissions() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResourceResponseDto resourceResponse = sendCreateResourceRequest();
    ResourceInOrganizationRequestDto resourceInOrganizationRequest =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), resourceResponse.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    sendResourceToOrganization(resourceInOrganizationRequest);
    assertResourcesInOrganizationSize(organization.getId().toString(), 1);
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                organization.getId(),
                resourceResponse.getId(),
                RESOURCE_QUANTITY));
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    HttpEntity<ProductRequestDto> request = new HttpEntity<>(productRequestDto);
    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            "/products/" + productInOrganizationResponse.getBody().getProducts().getFirst().getId(),
            HttpMethod.PUT,
            request,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void transferProductShouldThrowWhenUserHasNoProductTransferPermissionForBothOrganizations() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_QUANTITY));
    UUID productId = productInOrganizationResponse.getBody().getProducts().get(0).getId();
    OrganizationResponseDto createSecondOrganization = createOrganization();
    UUID organizationId = createSecondOrganization.getId();
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            "/products/" + productId + "/transfer/" + organizationId,
            HttpMethod.PUT,
            null,
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void transferProductShouldThrowWhenUserHasNoProductTransferPermissionForCurrentOrganization() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_QUANTITY));
    UUID productId = productInOrganizationResponse.getBody().getProducts().get(0).getId();
    OrganizationResponseDto createSecondOrganization = createOrganization();
    UUID organizationId = createSecondOrganization.getId();
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);
    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_PRODUCT_TRANSFER);
    RoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(deniedUser.getId(), createSecondOrganization.getId(), newRole.getId());

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            "/products/" + productId + "/transfer/" + organizationId,
            HttpMethod.PUT,
            null,
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void transferProductShouldThrowWhenUserHasNoProductTransferPermissionForNewOrganization() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> productInOrganizationResponse =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_QUANTITY));
    UUID productId = productInOrganizationResponse.getBody().getProducts().get(0).getId();
    OrganizationResponseDto createSecondOrganization = createOrganization();
    UUID organizationId = createSecondOrganization.getId();
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);
    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_PRODUCT_TRANSFER);
    RoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(deniedUser.getId(), organization.getId(), newRole.getId());

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            "/products/" + productId + "/transfer/" + organizationId,
            HttpMethod.PUT,
            null,
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getAllProductsByResourceWillReturnEmptyArrayWhenUserHasNoProductReadPermission() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    createProduct(
        setOwnerAndResourceToProductRequest(
            productRequestDto, organization.getId(), diamond.getId(), RESOURCE_QUANTITY));
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<List<ProductResponseDto>> response =
        testRestTemplate.exchange(
            getProductsUrlByResource(diamond.getId().toString()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllProductsByResourceWillReturnOnlyProductsUserHasProductReadPermissionFor() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    createProduct(
        setOwnerAndResourceToProductRequest(
            productRequestDto, organization.getId(), diamond.getId(), RESOURCE_QUANTITY));
    OrganizationResponseDto organization2 = createOrganization();
    sendResourceToOrganization(
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organization2.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    createProduct(
        setOwnerAndResourceToProductRequest(
            productRequestDto, organization2.getId(), diamond.getId(), RESOURCE_QUANTITY));
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_PRODUCT_READ);
    RoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(deniedUser.getId(), organization.getId(), newRole.getId());
    authenticateAs(deniedUser);

    ResponseEntity<List<ProductResponseDto>> response =
        testRestTemplate.exchange(
            getProductsUrlByResource(diamond.getId().toString()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getProductsByOwnerSuccessfully() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResponseEntity<ResourcesInOrganizationResponseDto>
        resourcesInOrganizationResponseDtoResponseEntity =
            sendResourceToOrganization(
                ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
                    organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> product =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_SALE_QUANTITY));
    User buyer = createAndPersistUser(createDifferentUserRequest());
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organization,
            buyer,
            product.getBody(),
            resourcesInOrganizationResponseDtoResponseEntity.getBody(),
            SALE_DISCOUNT);
    createSaleInOrganization(saleRequestDto);

    ResponseEntity<List<ProductResponseDto>> response =
        this.testRestTemplate.exchange(
            getProductByOwnerUrl(buyer.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
    assertEquals(
        response.getBody().getFirst().getId(), product.getBody().getProducts().getFirst().getId());
  }

  @Test
  void getProductsByOwnerShouldReturnEmptyArrayWhenUserHasNoProductReadAndSaleReadPermission() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResponseEntity<ResourcesInOrganizationResponseDto>
        resourcesInOrganizationResponseDtoResponseEntity =
            sendResourceToOrganization(
                ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
                    organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> product =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_SALE_QUANTITY));
    User buyer = createAndPersistUser(createDifferentUserRequest());
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organization,
            buyer,
            product.getBody(),
            resourcesInOrganizationResponseDtoResponseEntity.getBody(),
            SALE_DISCOUNT);
    createSaleInOrganization(saleRequestDto);
    authenticateAs(buyer);

    ResponseEntity<List<ProductResponseDto>> response =
        this.testRestTemplate.exchange(
            getProductByOwnerUrl(buyer.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getProductsByOwnerShouldReturnEmptyArrayWhenUserHasNoSaleReadPermission() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResponseEntity<ResourcesInOrganizationResponseDto>
        resourcesInOrganizationResponseDtoResponseEntity =
            sendResourceToOrganization(
                ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
                    organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> product =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_SALE_QUANTITY));
    User buyer = createAndPersistUser(createDifferentUserRequest());
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organization,
            buyer,
            product.getBody(),
            resourcesInOrganizationResponseDtoResponseEntity.getBody(),
            SALE_DISCOUNT);
    createSaleInOrganization(saleRequestDto);
    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_PRODUCT_READ);
    RoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(buyer.getId(), organization.getId(), newRole.getId());
    authenticateAs(buyer);

    ResponseEntity<List<ProductResponseDto>> response =
        this.testRestTemplate.exchange(
            getProductByOwnerUrl(buyer.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getProductsByOwnerShouldReturnEmptyArrayWhenUserHasNoProductReadPermission() {
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    ResponseEntity<ResourcesInOrganizationResponseDto>
        resourcesInOrganizationResponseDtoResponseEntity =
            sendResourceToOrganization(
                ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
                    organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> product =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_SALE_QUANTITY));
    User buyer = createAndPersistUser(createDifferentUserRequest());
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organization,
            buyer,
            product.getBody(),
            resourcesInOrganizationResponseDtoResponseEntity.getBody(),
            SALE_DISCOUNT);
    createSaleInOrganization(saleRequestDto);
    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_SALE_READ);
    RoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(buyer.getId(), organization.getId(), newRole.getId());
    authenticateAs(buyer);

    ResponseEntity<List<ProductResponseDto>> response =
        this.testRestTemplate.exchange(
            getProductByOwnerUrl(buyer.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getProductsByOwnerShouldReturnOnlyProductsUserHasPermissionFor() {
    OrganizationResponseDto newOrganization = createOrganization();
    addUserInOrganization(organization.getId(), userInOrganizationRequestDto);
    addUserInOrganization(newOrganization.getId(), userInOrganizationRequestDto);
    ResponseEntity<ResourcesInOrganizationResponseDto>
        resourcesInOrganizationResponseDtoResponseEntity =
            sendResourceToOrganization(
                ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
                    organization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ResourcesInOrganizationResponseDto>
        resourcesInNewOrganizationResponseDtoResponseEntity =
            sendResourceToOrganization(
                ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
                    newOrganization.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE));
    ResponseEntity<ProductsInOrganizationResponseDto> product =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto, organization.getId(), diamond.getId(), RESOURCE_SALE_QUANTITY));
    ResponseEntity<ProductsInOrganizationResponseDto> product2 =
        createProduct(
            setOwnerAndResourceToProductRequest(
                productRequestDto,
                newOrganization.getId(),
                diamond.getId(),
                RESOURCE_SALE_QUANTITY));
    User buyer = createAndPersistUser(createDifferentUserRequest());
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organization,
            buyer,
            product.getBody(),
            resourcesInOrganizationResponseDtoResponseEntity.getBody(),
            SALE_DISCOUNT);
    createSaleInOrganization(saleRequestDto);
    SaleRequestDto saleRequestDto2 =
        getSaleInOrganizationRequestDto(
            newOrganization,
            buyer,
            product2.getBody(),
            resourcesInNewOrganizationResponseDtoResponseEntity.getBody(),
            SALE_DISCOUNT);
    createSaleInOrganization(saleRequestDto2);
    Set<Permission> permissions =
        Set.of(Permission.ORGANIZATION_SALE_READ, Permission.ORGANIZATION_PRODUCT_READ);
    RoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(buyer.getId(), newOrganization.getId(), newRole.getId());
    authenticateAs(buyer);

    ResponseEntity<List<ProductResponseDto>> response =
        this.testRestTemplate.exchange(
            getProductByOwnerUrl(buyer.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ProductResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
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

    assertEquals(
        assertSize, organizationResourcesResponse.getBody().getResourcesAndQuantities().size());
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
        "/products", productRequestDto, ProductsInOrganizationResponseDto.class);
  }

  @Nullable
  private ResponseEntity<ProductsInOrganizationResponseDto> updateProduct(
      ProductRequestDto productRequestDto, String productId) {

    HttpEntity<ProductRequestDto> requestEntity = new HttpEntity<>(productRequestDto, headers);

    return this.testRestTemplate.exchange(
        "/products/" + productId,
        HttpMethod.PUT,
        requestEntity,
        ProductsInOrganizationResponseDto.class);
  }

  @Nullable
  private Diamond createDiamondInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getDiamondRequestDto();
    ResponseEntity<Diamond> createResource =
        this.testRestTemplate.postForEntity(getBaseResourceUrl(), resourceRequest, Diamond.class);

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

  private ResponseEntity<OrganizationSaleResponseDto> createSaleInOrganization(
      SaleRequestDto saleRequestDto) {
    return this.testRestTemplate.postForEntity(
        getBaseSaleUrl(), saleRequestDto, OrganizationSaleResponseDto.class);
  }
}
