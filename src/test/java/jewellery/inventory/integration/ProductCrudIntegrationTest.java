package jewellery.inventory.integration;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.SystemEventTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static jewellery.inventory.model.EventType.PRODUCT_CREATE;
import static jewellery.inventory.model.EventType.PRODUCT_DISASSEMBLY;
import static jewellery.inventory.model.EventType.PRODUCT_TRANSFER;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.PreciousStone;
import jewellery.inventory.repository.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

class ProductCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  @Autowired private UserRepository userRepository;

  @Autowired private SaleRepository saleRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private ResourceInUserRepository resourceInUserRepository;
  @Autowired private ResourceInProductRepository resourceInProductRepository;
  private User user;
  private PreciousStone preciousStone;
  private User differentUser;
  private ResourceInUserRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductRequestDto productRequestDto2;
  private ProductResponseDto productResponseDto;

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

  @Autowired private UserRepository userRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private ResourceInUserRepository resourceInUserRepository;
  @Autowired private ResourceInProductRepository resourceInProductRepository;

  private User user;
  private PreciousStone preciousStone;
  private User differentUser;
  private ResourceInUserRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductResponseDto productResponseDto;

  @BeforeEach
  void setUp() {
    cleanAllRepositories();

    user = createUserInDatabase(createTestUserRequest());
    preciousStone = createPreciousStoneInDatabase();
    resourceInUserRequestDto =
        getResourceInUserRequestDto(user, Objects.requireNonNull(preciousStone));
    differentUser = createUserInDatabase(createDifferentUserRequest());
    resourcesInUserResponseDto = getResourcesInUserResponseDto(resourceInUserRequestDto);
    productRequestDto =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), user);
    productRequestDto2 =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), user);
  }

  @Test
  void transferProductFailsWithNotFoundWhenIdsIncorrect() {
    UUID fakeId = UUID.randomUUID();

    ResponseEntity<ProductResponseDto> response =
        this.testRestTemplate.exchange(
            getBaseProductUrl() + "/" + fakeId + "/transfer/" + fakeId,
            HttpMethod.PUT,
            null,
            ProductResponseDto.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void transferProductSuccessfully() throws JsonProcessingException {
    ResponseEntity<ProductResponseDto> productResponse =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    productRequestDto2.setProductsContent(List.of(productResponse.getBody().getId()));
    ResponseEntity<ProductResponseDto> productResponse2 =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto2, ProductResponseDto.class);

    assertNotEquals(differentUser.getId(), productResponse.getBody().getOwner().getId());

    ResponseEntity<ProductResponseDto> resultResponse =
        this.testRestTemplate.exchange(
            getBaseProductUrl()
                + "/"
                + productResponse2.getBody().getId()
                + "/transfer/"
                + differentUser.getId(),
            HttpMethod.PUT,
            null,
            ProductResponseDto.class);

    assertEquals(
        differentUser.getId(),
        resultResponse.getBody().getProductsContent().get(0).getOwner().getId());
    assertNotNull(resultResponse.getBody());
    assertEquals(differentUser.getId(), resultResponse.getBody().getOwner().getId());
    assertEquals(HttpStatus.OK, resultResponse.getStatusCode());
    Map<String, Object> expectedEventSubPayload =
        getUpdateEventPayload(
            getEntityAsMap(productResponse.getBody()), getEntityAsMap(resultResponse.getBody()));

    assertEventWasLogged(
        this.testRestTemplate, getBaseSystemEventUrl(), PRODUCT_TRANSFER, expectedEventSubPayload);
  }

  @Test
  void createProductSuccessfully() throws JsonProcessingException {

    ResponseEntity<ProductResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);
    ProductResponseDto productResponseDto = response.getBody();

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(
        productRequestDto.getResourcesContent().get(0).getId(),
        productResponseDto.getResourcesContent().get(0).getResource().getId());
    assertEquals(productRequestDto.getOwnerId(), productResponseDto.getOwner().getId());
    assertEquals(productRequestDto.getProductionNumber(), productResponseDto.getProductionNumber());
    assertEquals(productRequestDto.getCatalogNumber(), productResponseDto.getCatalogNumber());

    Map<String, Object> expectedEventSubPayload =
        getCreateOrDeleteEventPayload(getEntityAsMap(productResponseDto));

    assertEventWasLogged(
        this.testRestTemplate, getBaseSystemEventUrl(), PRODUCT_CREATE, expectedEventSubPayload);
  }

  @Test
  void getProductSuccessfully() {
    productResponseDto = createProductWithRequest(productRequestDto);

    ResponseEntity<ProductResponseDto> response =
        this.testRestTemplate.getForEntity(
            getProductUrl(Objects.requireNonNull(productResponseDto).getId()),
            ProductResponseDto.class);

    ProductResponseDto responseBody = response.getBody();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(productRequestDto.getOwnerId(), responseBody.getOwner().getId());
    assertEquals(productRequestDto.getSalePrice(), responseBody.getSalePrice());
    assertEquals(
        productRequestDto.getResourcesContent().size(), responseBody.getResourcesContent().size());
    assertEquals(productRequestDto.getCatalogNumber(), responseBody.getCatalogNumber());
  }

  @Test
  void getAllProductsSuccessfully() {
    productResponseDto = createProductWithRequest(productRequestDto);

    ResponseEntity<List<ProductResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseProductUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertResponseMatchesCreatedRequest(response);
  }

  @Test
  void getProductsByOwnerSuccessfully() {
    createProductWithRequest(productRequestDto);

    ResponseEntity<List<ProductResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseProductUrl() + "/by-owner/" + user.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

    assertResponseMatchesCreatedRequest(response);
  }

  @Test
  void deleteProductSuccessfully() throws JsonProcessingException {
    productResponseDto = createProductWithRequest(productRequestDto);

    ResponseEntity<HttpStatus> response =
        this.testRestTemplate.exchange(
            getProductUrl(Objects.requireNonNull(productResponseDto).getId()),
            HttpMethod.DELETE,
            null,
            HttpStatus.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    ResponseEntity<ProductResponseDto> newResponse =
        this.testRestTemplate.getForEntity(
            getProductUrl(Objects.requireNonNull(productResponseDto).getId()),
            ProductResponseDto.class);

    assertEquals(HttpStatus.NOT_FOUND, newResponse.getStatusCode());
    Map<String, Object> expectedEventSubPayload =
        getCreateOrDeleteEventPayload(getEntityAsMap(productResponseDto));

    assertEventWasLogged(
        this.testRestTemplate, getBaseSystemEventUrl(), PRODUCT_DISASSEMBLY, expectedEventSubPayload);
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

  private void assertResponseMatchesCreatedRequest(
      ResponseEntity<List<ProductResponseDto>> response) {
    List<ProductResponseDto> productResponseDtos = response.getBody();
    ProductResponseDto currentResponse = productResponseDtos.get(0);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, productResponseDtos.size());
    assertEquals(productRequestDto.getOwnerId(), currentResponse.getOwner().getId());
    assertEquals(productRequestDto.getProductionNumber(), currentResponse.getProductionNumber());
  }

  @Nullable
  private ProductResponseDto createProductWithRequest(ProductRequestDto productRequestDto) {
    ResponseEntity<ProductResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    return response.getBody();
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
  private PreciousStone createPreciousStoneInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getPreciousStoneRequestDto();
    ResponseEntity<PreciousStone> createResource =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, PreciousStone.class);

    return createResource.getBody();
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
    resourceRepository.deleteAll();
    resourceInUserRepository.deleteAll();
    resourceInProductRepository.deleteAll();
    systemEventRepository.deleteAll();
  }
}
