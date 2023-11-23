package jewellery.inventory.integration;

import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDto;
import static jewellery.inventory.helper.SystemEventTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.model.EventType.SALE_CREATE;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.PreciousStone;
import jewellery.inventory.repository.*;
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

  private static final Double SALE_TOTAL_PRICE = 10000.0;
  private static final Double SALE_DISCOUNT = 10.0;
  private static final Double SALE_DISCOUNTED_PRICE = 9000.0;
  private User seller;
  private User buyer;
  private PreciousStone preciousStone;
  private ResourceInUserRequestDto resourceInUserRequestDto;
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
  void getAllSalesSuccessfully() {
    ResponseEntity<ProductResponseDto> productResponse =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse);

    ResponseEntity<SaleResponseDto> saleResponse =
        this.testRestTemplate.postForEntity(
            getBaseSaleUrl(), saleRequestDto, SaleResponseDto.class);

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

    ResponseEntity<ProductResponseDto> productResponse =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    productRequestDto2.setProductsContent(List.of(productResponse.getBody().getId()));
    ResponseEntity<ProductResponseDto> productResponse2 =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto2, ProductResponseDto.class);

    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse2);

    ResponseEntity<SaleResponseDto> saleResponse =
        this.testRestTemplate.postForEntity(
            getBaseSaleUrl(), saleRequestDto, SaleResponseDto.class);

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
    assertEquals(SALE_TOTAL_PRICE, saleResponse.getBody().getTotalPrice());
    assertEquals(SALE_DISCOUNT, saleResponse.getBody().getTotalDiscount());
    assertEquals(SALE_DISCOUNTED_PRICE, saleResponse.getBody().getTotalDiscountedPrice());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(saleResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(SALE_CREATE, expectedEventPayload);
  }

  @NotNull
  private static SaleRequestDto getSaleRequestDto(
      User seller, User buyer, ResponseEntity<ProductResponseDto> productResponse) {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setBuyerId(buyer.getId());
    saleRequestDto.setSellerId(seller.getId());
    saleRequestDto.setDate(Instant.now());
    ProductPriceDiscountRequestDto productPriceDiscountRequestDto =
        new ProductPriceDiscountRequestDto();
    productPriceDiscountRequestDto.setProductId(productResponse.getBody().getId());
    productPriceDiscountRequestDto.setSalePrice(SALE_TOTAL_PRICE);
    productPriceDiscountRequestDto.setDiscount(SALE_DISCOUNT);
    List<ProductPriceDiscountRequestDto> list = new ArrayList<>();
    list.add(productPriceDiscountRequestDto);
    saleRequestDto.setProducts(list);
    return saleRequestDto;
  }

  @NotNull
  private static ResourceInUserRequestDto getResourceInUserRequestDto(
      User user, PreciousStone preciousStone) {
    ResourceInUserRequestDto resourceInUserRequestDto = new ResourceInUserRequestDto();
    resourceInUserRequestDto.setUserId(user.getId());
    resourceInUserRequestDto.setResourceId(preciousStone.getId());
    resourceInUserRequestDto.setQuantity(20);
    return resourceInUserRequestDto;
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
