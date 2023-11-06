package jewellery.inventory.integration;

import static jewellery.inventory.helper.ProductTestHelper.getProductRequestDto;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Gemstone;
import jewellery.inventory.repository.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

  private String getProductUrl(UUID id) {
    return getBaseUrl() + "/products/" + id;
  }

  private String getBaseSaleUrl() {
    return getBaseUrl() + "/sales";
  }

  @Autowired private UserRepository userRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private ResourceInUserRepository resourceInUserRepository;
  @Autowired private ResourceInProductRepository resourceInProductRepository;

  private User seller;
  private User buyer;
  private Gemstone gemstone;
  private ResourceInUserRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;

  @BeforeEach
  void setUp() {
    cleanAllRepositories();
    seller = createUserInDatabase(createSellerUserRequest());
    buyer = createUserInDatabase(createBuyerUserRequest());
    gemstone = createGemstoneInDatabase();
    resourceInUserRequestDto =
        getResourceInUserRequestDto(seller, Objects.requireNonNull(gemstone));
    resourcesInUserResponseDto = getResourcesInUserResponseDto(resourceInUserRequestDto);
    productRequestDto =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), seller);
  }

  @Test
  void getAllSalesSuccessfully() {

    ResponseEntity<List<SaleResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseSaleUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertNotNull(response.getBody());
  }

  @Test
  void createSaleSuccessfully2() {

    ResponseEntity<ProductResponseDto> productResponse =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    SaleRequestDto saleRequestDto = getSaleRequestDto2(seller, buyer, productResponse);

    ResponseEntity<SaleResponseDto> saleResponse =
        this.testRestTemplate.postForEntity(
            getBaseSaleUrl(), saleRequestDto, SaleResponseDto.class);

    assertEquals(HttpStatus.CREATED, saleResponse.getStatusCode());
  }

  @NotNull
  private static SaleRequestDto getSaleRequestDto2(
      User seller, User buyer, ResponseEntity<ProductResponseDto> productResponse) {
    SaleRequestDto saleRequestDto = new SaleRequestDto();
    saleRequestDto.setBuyerId(buyer.getId());
    saleRequestDto.setSellerId(seller.getId());
    ProductPriceDiscountRequestDto productPriceDiscountRequestDto =
        new ProductPriceDiscountRequestDto();
    productPriceDiscountRequestDto.setProductId(productResponse.getBody().getId());
    productPriceDiscountRequestDto.setSalePrice(10000);
    productPriceDiscountRequestDto.setDiscount(10);
    List<ProductPriceDiscountRequestDto> list = new ArrayList<>();
    list.add(productPriceDiscountRequestDto);
    saleRequestDto.setProducts(list);
    return saleRequestDto;
  }

  @Nullable
  private Gemstone createGemstoneInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getGemstoneRequestDto();
    ResponseEntity<Gemstone> createResource =
        this.testRestTemplate.postForEntity(getBaseResourceUrl(), resourceRequest, Gemstone.class);

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
      User user, Gemstone gemstone) {
    ResourceInUserRequestDto resourceInUserRequestDto = new ResourceInUserRequestDto();
    resourceInUserRequestDto.setUserId(user.getId());
    resourceInUserRequestDto.setResourceId(gemstone.getId());
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
    userRepository.deleteAll();
    productRepository.deleteAll();
    resourceRepository.deleteAll();
    resourceInUserRepository.deleteAll();
    resourceInProductRepository.deleteAll();
  }
}
