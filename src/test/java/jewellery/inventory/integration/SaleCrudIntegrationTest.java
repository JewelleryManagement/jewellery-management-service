package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestUserInOrganizationRequest;
import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDto;
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
import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.PurchasedResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceInOrganizationTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Pearl;
import jewellery.inventory.model.resource.PreciousStone;
import jewellery.inventory.model.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

class SaleCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final BigDecimal RESOURCE_QUANTITY = getBigDecimal("100");
  private static final BigDecimal RESOURCE_PRICE = getBigDecimal("105");
  private static final BigDecimal SALE_TOTAL_PRICE = getBigDecimal("505");
  private static final BigDecimal SALE_DISCOUNT = getBigDecimal("10");
  private static final BigDecimal SALE_PRODUCT_DISCOUNTED_PRICE = getBigDecimal("454.50");
  private static final BigDecimal SALE_RESOURCE_DISCOUNTED_PRICE = getBigDecimal("45.45");
  private Organization organizationSeller;
  private User seller;
  private User buyer;
  private OrganizationSingleMemberResponseDto organizationSingleMemberResponseDto;
  private PreciousStone preciousStone;
  private ResponseEntity<Pearl> pearl;
  private ResourceInOrganizationRequestDto resourceInOrganizationRequestDto;
  private ResourcesInOrganizationResponseDto resourcesInOrganizationResponseDto;
  private ResourcePurchaseRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductRequestDto productRequestDto2;
  private ResourceInUserRequestDto pearlRequest;

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("resources", "availability");
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

  private String getBaseProductUrl() {
    return "/products";
  }

  private String getBaseSaleUrl() {
    return "/sales";
  }

  private String getBaseOrganizationSaleUrl() {
    return "/organizations/sales";
  }

  private String getBaseProductsInOrganizationUrl() {
    return buildUrl("organizations", "products");
  }

  private String getSaleReturnProductUrl(UUID productId) {
    return getBaseSaleUrl() + "/return-product/" + productId;
  }

  private String getSaleReturnResourceUrl(UUID saleId, UUID resourceId) {
    return getBaseSaleUrl() + "/" + saleId + "/return-resource/" + resourceId;
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
    organizationSingleMemberResponseDto =
        createUserInOrganization(
            organizationSeller, getTestUserInOrganizationRequest(seller.getId()));
    preciousStone = createPreciousStoneInDatabase();
    resourceInOrganizationRequestDto =
        ResourceInOrganizationTestHelper.createResourceInOrganizationRequestDto(
            organizationSeller.getId(), preciousStone.getId(), RESOURCE_QUANTITY, RESOURCE_PRICE);
    resourcesInOrganizationResponseDto =
        createResourceInOrganization(resourceInOrganizationRequestDto);
    resourceInUserRequestDto =
        getResourceInUserRequestDto(seller, Objects.requireNonNull(preciousStone));
    resourcesInUserResponseDto = getResourcesInUserResponseDto(resourceInUserRequestDto);
    productRequestDto =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), seller);
    productRequestDto2 =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), seller);
    pearl = createPearlInDatabase();
    pearlRequest = getResourceInUserRequestDto(seller, Objects.requireNonNull(pearl.getBody()));
    getResourcesInUserResponseDto(pearlRequest);
  }

  @Test
  void removeOrganizationSaleAfterReturnAllResourcesAndProductsFromSaleInOrganization() {
    productRequestDto.setOwnerId(organizationSeller.getId());
    ProductsInOrganizationResponseDto productResponse =
        createProductInOrganization(productRequestDto);
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller, buyer, productResponse, resourcesInOrganizationResponseDto);

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
    productRequestDto.setOwnerId(organizationSeller.getId());
    ProductsInOrganizationResponseDto productResponse =
        createProductInOrganization(productRequestDto);
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller, buyer, productResponse, resourcesInOrganizationResponseDto);

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
    productRequestDto.setOwnerId(organizationSeller.getId());
    ProductsInOrganizationResponseDto productResponse =
        createProductInOrganization(productRequestDto);
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller, buyer, productResponse, resourcesInOrganizationResponseDto);
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
    productRequestDto.setOwnerId(organizationSeller.getId());
    ProductsInOrganizationResponseDto productResponse =
        createProductInOrganization(productRequestDto);
    SaleRequestDto saleRequestDto =
        getSaleInOrganizationRequestDto(
            organizationSeller, buyer, productResponse, resourcesInOrganizationResponseDto);

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
  void returnResourceSuccessfully() throws JsonProcessingException {
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);
    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse, pearlRequest);
    ResponseEntity<SaleResponseDto> saleResponse = createSale(saleRequestDto);

    ResponseEntity<ResourceReturnResponseDto> response = getResourceReturnResponse(saleResponse);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getSaleAfter());
    assertEquals(pearl.getBody().getId(), response.getBody().getReturnedResource().getId());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(SALE_RETURN_RESOURCE, expectedEventPayload);
  }

  @Test
  void returnProductSuccessfully() throws JsonProcessingException {
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);
    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse, pearlRequest);
    saleRequestDto.setResources(new ArrayList<>());
    ResponseEntity<SaleResponseDto> saleResponse = createSale(saleRequestDto);

    assertEquals(
        saleRequestDto.getProducts().get(0).getProductId(), productResponse.getBody().getId());
    assertNotEquals(
        saleResponse.getBody().getProducts().get(0).getOwner(),
        productResponse.getBody().getOwner());

    ResponseEntity<ProductReturnResponseDto> response =
        returnProductFromSale(productResponse.getBody().getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNull(response.getBody().getSaleAfter());
    assertNull(response.getBody().getReturnedProduct().getPartOfSale());
    assertEquals(
        productResponse.getBody().getOwner(), response.getBody().getReturnedProduct().getOwner());
    assertEquals(response.getBody().getDate(), LocalDate.now());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(SALE_RETURN_PRODUCT, expectedEventPayload);
  }

  @Test
  void returnProductWillThrowsProductNotSoldException() {
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);

    ResponseEntity<ProductReturnResponseDto> response =
        returnProductFromSale(productResponse.getBody().getId());

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNull((productResponse.getBody()).getPartOfSale());
    assertNull((response.getBody()).getReturnedProduct());
  }

  @Test
  void getAllSalesSuccessfully() {
    SaleRequestDto saleRequestDto =
        getSaleRequestDto(seller, buyer, createProduct(productRequestDto), pearlRequest);

    ResponseEntity<SaleResponseDto> saleResponse = createSale(saleRequestDto);

    ResponseEntity<List<SaleResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseSaleUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertNotNull(response.getBody());
    assertEquals(response.getBody().size(), saleResponse.getBody().getProducts().size());
    assertEquals(
        response.getBody().get(0).getBuyer(),
        saleResponse.getBody().getProducts().get(0).getOwner());
    assertNotEquals(
        response.getBody().get(0).getSeller(),
        saleResponse.getBody().getProducts().get(0).getOwner());
  }

  @Test
  void createSaleShouldThrowWhenResourceNotOwned() {
    seller.setId(UUID.randomUUID());
    SaleRequestDto saleRequestDto =
        getSaleRequestDto(seller, buyer, createProduct(productRequestDto), pearlRequest);

    ResponseEntity<SaleResponseDto> saleResponse = createSale(saleRequestDto);

    assertEquals(HttpStatus.NOT_FOUND, saleResponse.getStatusCode());
  }

  @Test
  void createSaleWithResourceAndProductSuccessfully() throws JsonProcessingException {
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);

    productRequestDto2.setProductsContent(List.of(productResponse.getBody().getId()));
    ResponseEntity<ProductResponseDto> productResponse2 = createProduct(productRequestDto2);
    SaleRequestDto saleRequestDto =
        getSaleRequestDto(seller, buyer, productResponse2, pearlRequest);

    ResponseEntity<SaleResponseDto> saleResponse = createSale(saleRequestDto);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(saleRequestDto.getBuyerId(), saleResponse.getBody().getBuyer().getId());
    assertEquals(saleRequestDto.getSellerId(), saleResponse.getBody().getSeller().getId());
    assertEquals(saleRequestDto.getDate(), saleResponse.getBody().getDate());
    assertEquals(
        saleRequestDto.getResources().size(), saleResponse.getBody().getResources().size());
    assertEquals(saleRequestDto.getProducts().size(), saleResponse.getBody().getProducts().size());
    assertEquals(
        saleRequestDto.getProducts().get(0).getProductId(),
        saleResponse.getBody().getProducts().get(0).getId());

    assertEquals(
        SALE_TOTAL_PRICE.add(calculateTotalPriceOfResource(saleRequestDto)),
        saleResponse.getBody().getTotalPrice().setScale(2, RoundingMode.HALF_UP));
    assertEquals(
        SALE_DISCOUNT, saleResponse.getBody().getTotalDiscount().setScale(2, RoundingMode.HALF_UP));
    assertEquals(
        SALE_PRODUCT_DISCOUNTED_PRICE.add(SALE_RESOURCE_DISCOUNTED_PRICE),
        saleResponse.getBody().getTotalDiscountedPrice().setScale(2, RoundingMode.HALF_UP));

    assertEquals(
        saleResponse.getBody().getProducts().get(0).getPartOfSale(),
        saleResponse.getBody().getId());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(SALE_CREATE, expectedEventPayload);
  }

  @Test
  void createSaleWithResourcesOnlySuccessfully() throws JsonProcessingException {

    SaleRequestDto saleRequestDto =
        getSaleRequestDto(seller, buyer, createProduct(productRequestDto), pearlRequest);
    saleRequestDto.setProducts(new ArrayList<>());

    ResponseEntity<SaleResponseDto> saleResponse = createSale(saleRequestDto);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(
        saleRequestDto.getBuyerId(),
        Objects.requireNonNull(saleResponse.getBody()).getBuyer().getId());
    assertEquals(saleRequestDto.getSellerId(), saleResponse.getBody().getSeller().getId());
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

    systemEventTestHelper.assertEventWasLogged(SALE_CREATE, expectedEventPayload);
  }

  @Test
  void createSaleWithProductsOnlySuccessfully() throws JsonProcessingException {
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);

    productRequestDto2.setProductsContent(List.of(productResponse.getBody().getId()));
    ResponseEntity<ProductResponseDto> productResponse2 = createProduct(productRequestDto2);

    SaleRequestDto saleRequestDto =
        getSaleRequestDto(seller, buyer, productResponse2, pearlRequest);
    saleRequestDto.setResources(new ArrayList<>());
    ResponseEntity<SaleResponseDto> saleResponse = createSale(saleRequestDto);

    assertEquals(
        buyer.getId(),
        saleResponse.getBody().getProducts().get(0).getProductsContent().get(0).getOwner().getId());
    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(saleRequestDto.getBuyerId(), saleResponse.getBody().getBuyer().getId());
    assertEquals(saleRequestDto.getSellerId(), saleResponse.getBody().getSeller().getId());
    assertEquals(saleRequestDto.getDate(), saleResponse.getBody().getDate());
    assertEquals(saleRequestDto.getProducts().size(), saleResponse.getBody().getProducts().size());
    assertEquals(
        saleRequestDto.getProducts().get(0).getProductId(),
        saleResponse.getBody().getProducts().get(0).getId());
    assertEquals(SALE_TOTAL_PRICE, saleResponse.getBody().getTotalPrice().setScale(2));
    assertEquals(SALE_DISCOUNT, saleResponse.getBody().getTotalDiscount().setScale(2));
    assertEquals(SALE_DISCOUNT, saleResponse.getBody().getTotalDiscount().setScale(2));
    assertEquals(
        SALE_PRODUCT_DISCOUNTED_PRICE,
        saleResponse.getBody().getTotalDiscountedPrice().setScale(2));

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(SALE_CREATE, expectedEventPayload);
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
        getSaleRequestDto(
            seller, buyer, Objects.requireNonNull(createProduct(productRequestDto)), pearlRequest);
    saleRequestDto.setProducts(new ArrayList<>());

    createSale(saleRequestDto);

    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getPurchasedResourcesInUserUrl(buyer.getId()), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<PurchasedResourceQuantityResponseDto> resources =
        objectMapper.readValue(response.getBody(), new TypeReference<>() {});

    assertEquals(1, resources.size());
    for (PurchasedResourceQuantityResponseDto resource : resources) {
      assertEquals(
          resource.getResourceAndQuantity().getResource().getId(), pearl.getBody().getId());
    }
  }

  @Nullable
  private ResponseEntity<ProductResponseDto> createProduct(ProductRequestDto productRequestDto) {
    return this.testRestTemplate.postForEntity(
        getBaseProductUrl(), productRequestDto, ProductResponseDto.class);
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
  private ResponseEntity<SaleResponseDto> createSale(SaleRequestDto saleRequestDto) {
    return this.testRestTemplate.postForEntity(
        getBaseSaleUrl(), saleRequestDto, SaleResponseDto.class);
  }

  @Nullable
  private ResponseEntity<ProductReturnResponseDto> returnProductFromSale(UUID productId) {
    return this.testRestTemplate.exchange(
        getSaleReturnProductUrl(productId),
        HttpMethod.PUT,
        null,
        new ParameterizedTypeReference<>() {});
  }

  @NotNull
  private static SaleRequestDto getSaleRequestDto(
      User seller,
      User buyer,
      ResponseEntity<ProductResponseDto> productResponse,
      ResourceInUserRequestDto pearlInUserRequest) {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setBuyerId(buyer.getId());
    saleRequestDto.setSellerId(seller.getId());
    saleRequestDto.setDate(LocalDate.now());
    PurchasedResourceQuantityRequestDto purchasedResourceQuantityRequestDto =
        getPurchasedResourceInUserRequestDto(pearlInUserRequest);
    List<PurchasedResourceQuantityRequestDto> resources = new ArrayList<>();
    resources.add(purchasedResourceQuantityRequestDto);
    saleRequestDto.setResources(resources);
    ProductDiscountRequestDto productDiscountRequestDto = new ProductDiscountRequestDto();
    productDiscountRequestDto.setProductId(productResponse.getBody().getId());
    productDiscountRequestDto.setDiscount(SALE_DISCOUNT);
    List<ProductDiscountRequestDto> list = new ArrayList<>();
    list.add(productDiscountRequestDto);
    saleRequestDto.setProducts(list);
    return saleRequestDto;
  }

  @NotNull
  private static PurchasedResourceQuantityRequestDto getPurchasedResourceInUserRequestDto(
      ResourceInUserRequestDto pearlRequest) {
    return PurchasedResourceQuantityRequestDto.builder()
        .resourceAndQuantity(getResourceQuantityRequestDto(pearlRequest))
        .discount(SALE_DISCOUNT)
        .build();
  }

  @NotNull
  private static ResourceQuantityRequestDto getResourceQuantityRequestDto(
      ResourceInUserRequestDto pearlInUserRequest) {
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    resourceQuantityRequestDto.setResourceId(pearlInUserRequest.getResourceId());
    resourceQuantityRequestDto.setQuantity(BigDecimal.ONE);
    return resourceQuantityRequestDto;
  }

  @NotNull
  private static ResourcePurchaseRequestDto getResourceInUserRequestDto(
      User user, Resource resource) {
    return ResourcePurchaseRequestDto.builder()
        .userId(user.getId())
        .resourceId(resource.getId())
        .quantity(getBigDecimal("20"))
        .dealPrice(getBigDecimal("100"))
        .build();
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
  private ResponseEntity<Pearl> createPearlInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getPearlRequestDto();
    return this.testRestTemplate.postForEntity(getBaseResourceUrl(), resourceRequest, Pearl.class);
  }

  @Nullable
  private ResourcesInUserResponseDto getResourcesInUserResponseDto(
      ResourceInUserRequestDto resourceInUserRequestDto) {
    ResponseEntity<ResourcesInUserResponseDto> createResourceInUser =
        this.testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(),
            resourceInUserRequestDto,
            ResourcesInUserResponseDto.class);

    return createResourceInUser.getBody();
  }

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequest) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, User.class);

    return createUser.getBody();
  }

  @NotNull
  private BigDecimal calculateTotalPriceOfResource(SaleRequestDto saleRequestDto) {
    return Objects.requireNonNull(pearl.getBody())
        .getPricePerQuantity()
        .multiply(saleRequestDto.getResources().get(0).getResourceAndQuantity().getQuantity());
  }

  private ResponseEntity<ResourceReturnResponseDto> getResourceReturnResponse(
      ResponseEntity<SaleResponseDto> saleResponse) {
    return this.testRestTemplate.exchange(
        getSaleReturnResourceUrl(saleResponse.getBody().getId(), pearl.getBody().getId()),
        HttpMethod.PUT,
        null,
        ResourceReturnResponseDto.class);
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

  @NotNull
  private static SaleRequestDto getSaleInOrganizationRequestDto(
      Organization seller,
      User buyer,
      ProductsInOrganizationResponseDto productsInOrganizationResponseDto,
      ResourcesInOrganizationResponseDto resourcesInOrganizationResponseDto) {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setBuyerId(buyer.getId());
    saleRequestDto.setSellerId(seller.getId());
    saleRequestDto.setDate(LocalDate.now());
    PurchasedResourceQuantityRequestDto purchasedResourceQuantityRequestDto =
        new PurchasedResourceQuantityRequestDto();
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    resourceQuantityRequestDto.setResourceId(
        resourcesInOrganizationResponseDto
            .getResourcesAndQuantities()
            .get(0)
            .getResource()
            .getId());
    resourceQuantityRequestDto.setQuantity(BigDecimal.ONE);
    purchasedResourceQuantityRequestDto.setResourceAndQuantity(resourceQuantityRequestDto);
    purchasedResourceQuantityRequestDto.setDiscount(SALE_DISCOUNT);
    List<PurchasedResourceQuantityRequestDto> resources = new ArrayList<>();
    resources.add(purchasedResourceQuantityRequestDto);
    saleRequestDto.setResources(resources);
    ProductDiscountRequestDto productDiscountRequestDto = new ProductDiscountRequestDto();
    productDiscountRequestDto.setProductId(
        productsInOrganizationResponseDto.getProducts().get(0).getId());
    productDiscountRequestDto.setDiscount(SALE_DISCOUNT);
    List<ProductDiscountRequestDto> list = new ArrayList<>();
    list.add(productDiscountRequestDto);
    saleRequestDto.setProducts(list);
    return saleRequestDto;
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
