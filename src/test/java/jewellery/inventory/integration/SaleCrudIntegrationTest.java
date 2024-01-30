package jewellery.inventory.integration;

import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDto;
import static jewellery.inventory.helper.SystemEventTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.model.EventType.SALE_CREATE;
import static jewellery.inventory.model.EventType.SALE_RETURN_PRODUCT;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.PreciousStone;
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

  private static final BigDecimal SALE_TOTAL_PRICE = getBigDecimal("505");
  private static final BigDecimal SALE_DISCOUNT = getBigDecimal("10");
  private static final BigDecimal SALE_DISCOUNTED_PRICE = getBigDecimal("454.50");
  private User seller;
  private User buyer;
  private PreciousStone preciousStone;
  private ResourcePurchaseRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductRequestDto productRequestDto2;

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("resources", "availability");
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

  private String getSaleReturnProductUrl(UUID productId) {
    return getBaseSaleUrl() + "/return-product/" + productId;
  }

  @BeforeEach
  void setUp() {
    seller = createUserInDatabase(createTestUserRequest());
    buyer = createUserInDatabase(createDifferentUserRequest());
    preciousStone = createPreciousStoneInDatabase();
    resourceInUserRequestDto =
        getResourceInUserRequestDto(seller, Objects.requireNonNull(preciousStone));
    resourcesInUserResponseDto = getResourcesInUserResponseDto(resourceInUserRequestDto);
    productRequestDto =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), seller);
    productRequestDto2 =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), seller);
  }

  @Test
  void returnProductSuccessfully() throws JsonProcessingException {
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);

    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse);

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
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);

    SaleRequestDto saleRequestDto =
        getSaleRequestDto(seller, buyer, createProduct(productRequestDto));

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
  void createSaleSuccessfully() throws JsonProcessingException {
    ResponseEntity<ProductResponseDto> productResponse = createProduct(productRequestDto);

    productRequestDto2.setProductsContent(List.of(productResponse.getBody().getId()));
    ResponseEntity<ProductResponseDto> productResponse2 = createProduct(productRequestDto2);

    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse2);

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
    assertEquals(SALE_TOTAL_PRICE,saleResponse.getBody().getTotalPrice().setScale(2));
    assertEquals(
        SALE_DISCOUNT, saleResponse.getBody().getTotalDiscount().setScale(2));
    assertEquals(
        SALE_DISCOUNTED_PRICE, saleResponse.getBody().getTotalDiscountedPrice().setScale(2));

        Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(SALE_CREATE, expectedEventPayload);
  }

  @Nullable
  private ResponseEntity<ProductResponseDto> createProduct(ProductRequestDto productRequestDto) {
    return this.testRestTemplate.postForEntity(
        getBaseProductUrl(), productRequestDto, ProductResponseDto.class);
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
      User seller, User buyer, ResponseEntity<ProductResponseDto> productResponse) {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setBuyerId(buyer.getId());
    saleRequestDto.setSellerId(seller.getId());
    saleRequestDto.setDate(LocalDate.now());
    ProductDiscountRequestDto productDiscountRequestDto =
        new ProductDiscountRequestDto();
    productDiscountRequestDto.setProductId(productResponse.getBody().getId());
    productDiscountRequestDto.setDiscount(SALE_DISCOUNT);
    List<ProductDiscountRequestDto> list = new ArrayList<>();
    list.add(productDiscountRequestDto);
    saleRequestDto.setProducts(list);
    return saleRequestDto;
  }

  @NotNull
  private static ResourcePurchaseRequestDto getResourceInUserRequestDto(
      User user, PreciousStone preciousStone) {
    ResourcePurchaseRequestDto resourcePurchaseRequestDto = new ResourcePurchaseRequestDto();
    resourcePurchaseRequestDto.setUserId(user.getId());
    resourcePurchaseRequestDto.setResourceId(preciousStone.getId());
    resourcePurchaseRequestDto.setQuantity(getBigDecimal("20"));
    resourcePurchaseRequestDto.setDealPrice(getBigDecimal("100"));
    return resourcePurchaseRequestDto;
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
}
