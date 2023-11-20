package jewellery.inventory.integration;

import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDto;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.PreciousStone;
import jewellery.inventory.repository.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

class SaleIntegrationTest extends AuthenticatedIntegrationTestBase {
  private String getBaseUrl() {
    return BASE_URL_PATH + port;
  }

  private String buildUrl(String... paths) {
    return getBaseUrl() + "/" + String.join("/", paths);
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("resources", "availability");
  }

  private String getBaseResourceUrl() {
    return getBaseUrl() + "/resources";
  }

  private String getBaseUserUrl() {
    return getBaseUrl() + "/users";
  }

  private String getBaseProductUrl() {
    return getBaseUrl() + "/products";
  }

  private String getBaseSaleUrl() {
    return getBaseUrl() + "/sales";
  }

  @Autowired private UserRepository userRepository;
  @Autowired private SaleRepository saleRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private ResourceInUserRepository resourceInUserRepository;
  @Autowired private ResourceInProductRepository resourceInProductRepository;

  private User seller;
  private User buyer;
  private PreciousStone preciousStone;

  private ResourceInUserRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;

  @BeforeEach
  void setUp() {
    cleanAllRepositories();
    seller = createUserInDatabase(createTestUserRequest());
    buyer = createUserInDatabase(createDifferentUserRequest());
    preciousStone = createPreciousStoneInDatabase();
    resourceInUserRequestDto =
        getResourceInUserRequestDto(seller, Objects.requireNonNull(preciousStone));
    resourcesInUserResponseDto = getResourcesInUserResponseDto(resourceInUserRequestDto);
    productRequestDto =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), seller);
  }

  @Test
  void returnProductSuccessfully() {
    ResponseEntity<ProductResponseDto> productResponse =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse);

    ResponseEntity<SaleResponseDto> saleResponse =
        this.testRestTemplate.postForEntity(
            getBaseSaleUrl(), saleRequestDto, SaleResponseDto.class);

    ResponseEntity<ProductResponseDto> response =
        this.testRestTemplate.exchange(
            getBaseSaleUrl()
                + "/return-product/"
                + Objects.requireNonNull(productResponse.getBody()).getId(),
            HttpMethod.PUT,
            null,
            new ParameterizedTypeReference<>() {});

    assertNotNull(response.getBody());
    Assertions.assertNull(response.getBody().getPartOfSale());
    Assertions.assertNotEquals(
        response.getBody().getOwner().getId(),
        Objects.requireNonNull(saleResponse.getBody()).getBuyer().getId());
    assertEquals(response.getBody().getOwner().getId(), saleResponse.getBody().getSeller().getId());
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
  void createSaleSuccessfully() {

    ResponseEntity<ProductResponseDto> productResponse =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    SaleRequestDto saleRequestDto = getSaleRequestDto(seller, buyer, productResponse);

    ResponseEntity<SaleResponseDto> saleResponse =
        this.testRestTemplate.postForEntity(
            getBaseSaleUrl(), saleRequestDto, SaleResponseDto.class);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
    assertEquals(saleRequestDto.getBuyerId(), saleResponse.getBody().getBuyer().getId());
    assertEquals(saleRequestDto.getSellerId(), saleResponse.getBody().getSeller().getId());
    assertEquals(saleRequestDto.getProducts().size(), saleResponse.getBody().getProducts().size());
    assertEquals(
        saleRequestDto.getProducts().get(0).getProductId(),
        saleResponse.getBody().getProducts().get(0).getId());
  }

  @NotNull
  private static SaleRequestDto getSaleRequestDto(
      User seller, User buyer, ResponseEntity<ProductResponseDto> productResponse) {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setBuyerId(buyer.getId());
    saleRequestDto.setSellerId(seller.getId());
    saleRequestDto.setDate(Date.from(Instant.now()));
    ProductPriceDiscountRequestDto productPriceDiscountRequestDto =
        new ProductPriceDiscountRequestDto();
    productPriceDiscountRequestDto.setProductId(productResponse.getBody().getId());
    productPriceDiscountRequestDto.setSalePrice(10000.0);
    productPriceDiscountRequestDto.setDiscount(10.0);
    List<ProductPriceDiscountRequestDto> list = new ArrayList<>();
    list.add(productPriceDiscountRequestDto);
    saleRequestDto.setProducts(list);
    return saleRequestDto;
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
  private User createUserInDatabase(UserRequestDto userRequest) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, User.class);

    return createUser.getBody();
  }

  private void cleanAllRepositories() {
    productRepository.deleteAll();
    saleRepository.deleteAll();
    userRepository.deleteAll();
    productRepository.deleteAll();
    resourceRepository.deleteAll();
    resourceInUserRepository.deleteAll();
    resourceInProductRepository.deleteAll();
  }
}
