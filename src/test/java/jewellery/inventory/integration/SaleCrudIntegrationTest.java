package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestUserInOrganizationRequest;
import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDtoForOrganization;
import static jewellery.inventory.helper.SaleTestHelper.getSaleInOrganizationRequestDto;
import static jewellery.inventory.helper.SystemEventTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.model.EventType.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceInOrganizationTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Diamond;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SaleCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("100");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105");
  private static final BigDecimal SALE_DISCOUNT = getBigDecimal("10");
  private static final BigDecimal SALE_RESOURCE_DISCOUNTED_PRICE = getBigDecimal("45.45");
  private Organization organizationSeller;
  private User seller;
  private User buyer;
  private Diamond diamond;
  private ResourceInOrganizationRequestDto resourceInOrganizationRequestDto;
  private ResourcesInOrganizationResponseDto resourcesInOrganizationResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductRequestDto productRequestDto2;
  private ProductsInOrganizationResponseDto productResponse;

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseResourceInOrganizationAvailabilityUrl() {
    return buildUrl("organizations", "resources-availability");
  }

  private String getBaseResourceUrl() {
    return "/resources";
  }

  private String getBaseUserUrl() {
    return "/users";
  }

  private String getBaseOrganizationSaleUrl() {
    return "/organizations/sales";
  }

  private String getBaseProductsInOrganizationUrl() {
    return buildUrl("organizations", "products");
  }

  private String getPurchasedResourcesInUserUrl(UUID userId) {
    return "/resources/availability/purchased/" + userId;
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private String getOrganizationUsersUrl(UUID organizationId) {
    return "/organizations/" + organizationId + "/users";
  }

  private String getOrganizationSaleReturnResourceUrl(UUID saleId, UUID resourceId) {
    return "/organizations/sales/" + saleId + "/return-resource/" + resourceId;
  }

  private String getOrganizationSaleReturnProductUrl(UUID productId) {
    return "/organizations/sales/return-product/" + productId;
  }

  @BeforeEach
  void setUp() {
    organizationSeller =
        createOrganizationInDatabase(OrganizationTestHelper.getTestOrganizationRequest());
    seller = createUserInDatabase(createTestUserRequest());
    buyer = createUserInDatabase(createDifferentUserRequest());
    createUserInOrganization(organizationSeller, getTestUserInOrganizationRequest(seller.getId()));
    diamond = createDiamondInDatabase();
    resourceInOrganizationRequestDto =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationSeller.getId(), diamond.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    resourcesInOrganizationResponseDto =
        createResourceInOrganization(resourceInOrganizationRequestDto);
    productRequestDto =
        getProductRequestDtoForOrganization(
            seller,
            organizationSeller.getId(),
            resourcesInOrganizationResponseDto
                .getResourcesAndQuantities()
                .getFirst()
                .getResource()
                .getId(),
            getBigDecimal("20"));
    productRequestDto2 =
        getProductRequestDtoForOrganization(
            seller,
            organizationSeller.getId(),
            resourcesInOrganizationResponseDto
                .getResourcesAndQuantities()
                .getFirst()
                .getResource()
                .getId(),
            getBigDecimal("20"));
    productResponse = createProductInOrganization(productRequestDto);
  }

  @Test
  void removeOrganizationSaleAfterReturnAllResourcesAndProductsFromSaleInOrganization() {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);
    ResponseEntity<ResourceReturnResponseDto> resourceReturnResponse =
        createReturnResourceResponse(
            saleRequestDto, Objects.requireNonNull(saleResponse.getBody()));

    ResponseEntity<ProductReturnResponseDto> productReturnResponse =
        this.testRestTemplate.exchange(
            getOrganizationSaleReturnProductUrl(productResponse.getProducts().get(0).getId()),
            HttpMethod.PUT,
            null,
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, productReturnResponse.getStatusCode());
    assertNotNull(productReturnResponse.getBody());
    assertNull(productReturnResponse.getBody().getSaleAfter());
    assertEquals(
        saleResponse.getBody().getProducts().get(0).getId(),
        productReturnResponse.getBody().getReturnedProduct().getId());
    assertEquals(
        saleResponse.getBody().getResources().get(0).getResourceAndQuantity().getResource().getId(),
        resourceReturnResponse.getBody().getReturnedResource().getId());
  }

  @Test
  void returnProductFromSaleInOrganizationSuccessfully() throws JsonProcessingException {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    ResponseEntity<ProductReturnResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationSaleReturnProductUrl(productResponse.getProducts().get(0).getId()),
            HttpMethod.PUT,
            null,
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getSaleAfter());
    assertEquals(
        saleResponse.getBody().getProducts().get(0).getId(),
        response.getBody().getReturnedProduct().getId());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_SALE_RETURN_PRODUCT, expectedEventPayload);
  }

  @Test
  void returnResourceFromSaleInOrganizationSuccessfully() throws JsonProcessingException {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    ResponseEntity<ResourceReturnResponseDto> response =
        createReturnResourceResponse(
            saleRequestDto, Objects.requireNonNull(saleResponse.getBody()));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getSaleAfter());
    assertEquals(
        saleResponse.getBody().getResources().get(0).getResourceAndQuantity().getResource().getId(),
        response.getBody().getReturnedResource().getId());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_SALE_RETURN_RESOURCE, expectedEventPayload);
  }

  @Test
  void createSaleInOrganizationSuccessfully() throws JsonProcessingException {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);

    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(saleRequestDto.getBuyerId(), saleResponse.getBody().getBuyer().getId());
    assertEquals(
        saleRequestDto.getSellerId(), saleResponse.getBody().getOrganizationSeller().getId());
    assertEquals(saleRequestDto.getDate(), saleResponse.getBody().getDate());
    assertEquals(
        saleRequestDto.getResources().size(), saleResponse.getBody().getResources().size());
    assertEquals(saleRequestDto.getProducts().size(), saleResponse.getBody().getProducts().size());
    assertEquals(
        saleRequestDto.getProducts().get(0).getProductId(),
        saleResponse.getBody().getProducts().get(0).getId());
    assertEquals(
        saleResponse.getBody().getProducts().get(0).getPartOfSale(),
        saleResponse.getBody().getId());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_CREATE_SALE, expectedEventPayload);
  }

  @Test
  void getAllOrganizationSalesSuccessfully() throws JsonProcessingException {
    createSaleInOrganizationSuccessfully();
    createSaleWithResourcesOnlySuccessfully();

    ResponseEntity<List<OrganizationSaleResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseOrganizationSaleUrl(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(
        2,
        response.getBody().size(),
        "Expected 2 sale in get sales response. Got " + response.getBody().size());
  }

  @Test
  void returnResourceSuccessfully() throws JsonProcessingException {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    ResponseEntity<ResourceReturnResponseDto> response =
        createReturnResourceResponse(
            saleRequestDto, Objects.requireNonNull(saleResponse.getBody()));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getSaleAfter());
    assertEquals(diamond.getId(), response.getBody().getReturnedResource().getId());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_SALE_RETURN_RESOURCE, expectedEventPayload);
  }

  @Test
  void returnProductSuccessfully() throws JsonProcessingException {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    saleRequestDto.setResources(new ArrayList<>());
    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    assertEquals(
        saleRequestDto.getProducts().getFirst().getProductId(),
        productResponse.getProducts().getFirst().getId());
    assertNotEquals(
        saleResponse.getBody().getProducts().getFirst().getOwner(),
        productResponse.getProducts().getFirst().getOwner());

    ResponseEntity<ProductReturnResponseDto> response =
        returnProductFromSale(productResponse.getProducts().getFirst().getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNull(response.getBody().getSaleAfter());
    assertNull(response.getBody().getReturnedProduct().getPartOfSale());
    assertEquals(response.getBody().getDate(), LocalDate.now());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_SALE_RETURN_PRODUCT, expectedEventPayload);
  }

  @Test
  void returnProductWillThrowsProductNotSoldException() {
    ResponseEntity<ProductReturnResponseDto> response =
        returnProductFromSale(productResponse.getProducts().getFirst().getId());

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNull(productResponse.getProducts().getFirst().getPartOfSale());
    assertNull((response.getBody()).getReturnedProduct());
  }

  @Test
  void getAllSalesSuccessfully() {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);

    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertNotNull(saleResponse.getBody());
    assertEquals(saleResponse.getBody().getBuyer().getId(), saleRequestDto.getBuyerId());
    assertNotEquals(saleResponse.getBody().getBuyer().getId(), saleRequestDto.getSellerId());
  }

  @Test
  void createSaleShouldThrowWhenResourceNotOwned() {
    organizationSeller.setId(UUID.randomUUID());
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);

    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    assertEquals(HttpStatus.NOT_FOUND, saleResponse.getStatusCode());
  }

  @Test
  void createSaleWithResourceAndProductSuccessfully() throws JsonProcessingException {
    productRequestDto2.setProductsContent(
        List.of(productResponse.getProducts().getFirst().getId()));
    ProductsInOrganizationResponseDto productResponse2 =
        createProductInOrganization(productRequestDto2);
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse2,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);

    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(saleRequestDto.getBuyerId(), saleResponse.getBody().getBuyer().getId());
    assertEquals(
        saleRequestDto.getSellerId(), saleResponse.getBody().getOrganizationSeller().getId());
    assertEquals(saleRequestDto.getDate(), saleResponse.getBody().getDate());
    assertEquals(
        saleRequestDto.getResources().size(), saleResponse.getBody().getResources().size());
    assertEquals(saleRequestDto.getProducts().size(), saleResponse.getBody().getProducts().size());
    assertEquals(
        saleRequestDto.getProducts().get(0).getProductId(),
        saleResponse.getBody().getProducts().get(0).getId());

    assertEquals(
        calculateTotalPriceOfResource(saleRequestDto)
            .add(productResponse2.getProducts().getFirst().getSalePrice()),
        saleResponse.getBody().getTotalPrice().setScale(2, RoundingMode.HALF_UP));
    assertEquals(
        SALE_DISCOUNT, saleResponse.getBody().getTotalDiscount().setScale(2, RoundingMode.HALF_UP));

    assertEquals(
        saleResponse.getBody().getProducts().get(0).getPartOfSale(),
        saleResponse.getBody().getId());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_CREATE_SALE, expectedEventPayload);
  }

  @Test
  void createSaleWithResourcesOnlySuccessfully() throws JsonProcessingException {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    saleRequestDto.setProducts(new ArrayList<>());

    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(
        saleRequestDto.getBuyerId(),
        Objects.requireNonNull(saleResponse.getBody()).getBuyer().getId());
    assertEquals(
        saleRequestDto.getSellerId(), saleResponse.getBody().getOrganizationSeller().getId());
    assertEquals(saleRequestDto.getDate(), saleResponse.getBody().getDate());
    assertEquals(
        saleRequestDto.getResources().size(), saleResponse.getBody().getResources().size());
    assertEquals(
        saleRequestDto.getResources().get(0).getResourceAndQuantity().getResourceId(),
        saleResponse
            .getBody()
            .getResources()
            .get(0)
            .getResourceAndQuantity()
            .getResource()
            .getId());

    assertEquals(
        calculateTotalPriceOfResource(saleRequestDto), saleResponse.getBody().getTotalPrice());

    assertEquals(getBigDecimal("10"), saleResponse.getBody().getTotalDiscount().setScale(2));
    assertEquals(
        SALE_RESOURCE_DISCOUNTED_PRICE,
        saleResponse.getBody().getTotalDiscountedPrice().setScale(2));

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_CREATE_SALE, expectedEventPayload);
  }

  @Test
  void createSaleWithProductsOnlySuccessfully() throws JsonProcessingException {
    productRequestDto2.setProductsContent(
        List.of(productResponse.getProducts().getFirst().getId()));
    ProductsInOrganizationResponseDto productResponse2 =
        createProductInOrganization(productRequestDto2);

    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse2,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    saleRequestDto.setResources(new ArrayList<>());
    ResponseEntity<OrganizationSaleResponseDto> saleResponse =
        createSaleInOrganization(saleRequestDto);

    assertEquals(
        buyer.getId(),
        saleResponse.getBody().getProducts().get(0).getProductsContent().get(0).getOwner().getId());
    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(saleRequestDto.getBuyerId(), saleResponse.getBody().getBuyer().getId());
    assertEquals(
        saleRequestDto.getSellerId(), saleResponse.getBody().getOrganizationSeller().getId());
    assertEquals(saleRequestDto.getDate(), saleResponse.getBody().getDate());
    assertEquals(saleRequestDto.getProducts().size(), saleResponse.getBody().getProducts().size());
    assertEquals(
        saleRequestDto.getProducts().get(0).getProductId(),
        saleResponse.getBody().getProducts().get(0).getId());
    assertEquals(
        productResponse2.getProducts().getFirst().getSalePrice(),
        saleResponse.getBody().getTotalPrice().setScale(2, RoundingMode.HALF_UP));
    assertEquals(SALE_DISCOUNT, saleResponse.getBody().getTotalDiscount().setScale(2));

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_CREATE_SALE, expectedEventPayload);
  }

  @Test
  void testGetAllPurchasedResourcesShouldThrowWhenUserNotFound() {
    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getPurchasedResourcesInUserUrl(UUID.randomUUID()), String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testGetAllPurchasedResourcesSuccessfully() throws JsonProcessingException {
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller,
            buyer,
            productResponse,
            resourcesInOrganizationResponseDto,
            SALE_DISCOUNT);
    saleRequestDto.setProducts(new ArrayList<>());

    createSaleInOrganization(saleRequestDto);

    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getPurchasedResourcesInUserUrl(buyer.getId()), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PurchasedResourceQuantityResponseDto> resources =
        objectMapper.readValue(response.getBody(), new TypeReference<>() {});

    assertEquals(1, resources.size());
    for (PurchasedResourceQuantityResponseDto resource : resources) {
      assertEquals(resource.getResourceAndQuantity().getResource().getId(), diamond.getId());
    }
  }

  private ProductsInOrganizationResponseDto createProductInOrganization(
      ProductRequestDto productRequestDto) {
    ResponseEntity<ProductsInOrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductsInOrganizationUrl(),
            productRequestDto,
            ProductsInOrganizationResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    return response.getBody();
  }

  @Nullable
  private ResponseEntity<ProductReturnResponseDto> returnProductFromSale(UUID productId) {
    return this.testRestTemplate.exchange(
        getOrganizationSaleReturnProductUrl(productId),
        HttpMethod.PUT,
        null,
        new ParameterizedTypeReference<>() {});
  }

  @Nullable
  private Diamond createDiamondInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getDiamondRequestDto();
    ResponseEntity<Diamond> createResource =
        this.testRestTemplate.postForEntity(getBaseResourceUrl(), resourceRequest, Diamond.class);

    return createResource.getBody();
  }

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequest) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, User.class);

    return createUser.getBody();
  }

  @NotNull
  private BigDecimal calculateTotalPriceOfResource(SaleRequestDto saleRequestDto) {
    return Objects.requireNonNull(diamond)
        .getPricePerQuantity()
        .multiply(saleRequestDto.getResources().get(0).getResourceAndQuantity().getQuantity());
  }

  private Organization createOrganizationInDatabase(
      OrganizationRequestDto testOrganizationRequest) {
    ResponseEntity<Organization> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), testOrganizationRequest, Organization.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    return response.getBody();
  }

  private OrganizationSingleMemberResponseDto createUserInOrganization(
      Organization organizationSeller, UserInOrganizationRequestDto userInOrganizationRequestDto) {
    return this.testRestTemplate
        .postForEntity(
            getOrganizationUsersUrl(organizationSeller.getId()),
            userInOrganizationRequestDto,
            OrganizationSingleMemberResponseDto.class)
        .getBody();
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

  private ResponseEntity<OrganizationSaleResponseDto> createSaleInOrganization(
      SaleRequestDto saleRequestDto) {
    return this.testRestTemplate.postForEntity(
        getBaseOrganizationSaleUrl(), saleRequestDto, OrganizationSaleResponseDto.class);
  }

  private ResponseEntity<ResourceReturnResponseDto> createReturnResourceResponse(
      SaleRequestDto saleRequestDto, OrganizationSaleResponseDto saleResponse) {
    return this.testRestTemplate.exchange(
        getOrganizationSaleReturnResourceUrl(
            saleResponse.getId(),
            saleRequestDto.getResources().get(0).getResourceAndQuantity().getResourceId()),
        HttpMethod.PUT,
        null,
        ResourceReturnResponseDto.class);
  }
}
