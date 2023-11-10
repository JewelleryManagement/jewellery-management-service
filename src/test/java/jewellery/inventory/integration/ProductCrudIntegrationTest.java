package jewellery.inventory.integration;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Gemstone;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ImageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class ProductCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

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

  private String getBaseProductImageUrl(UUID productId) {
    return getBaseProductUrl() + "/" + productId + "/picture";
  }

  @Autowired private UserRepository userRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private ResourceRepository resourceRepository;
  @Autowired private ResourceInUserRepository resourceInUserRepository;
  @Autowired private ResourceInProductRepository resourceInProductRepository;
  @MockBean private ImageService imageService;

  private User user;
  private User differentUser;
  private Gemstone gemstone;
  private ResourceInUserRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductResponseDto productResponseDto;
  private HttpEntity<MultiValueMap<String, Object>> multipartRequest;

  @BeforeEach
  void setUp() {
    cleanAllRepositories();
    user = createUserInDatabase(createTestUserRequest());
    differentUser = createUserInDatabase(createDifferentUserRequest());
    gemstone = createGemstoneInDatabase();
    resourceInUserRequestDto = getResourceInUserRequestDto(user, Objects.requireNonNull(gemstone));
    resourcesInUserResponseDto = getResourcesInUserResponseDto(resourceInUserRequestDto);
    productRequestDto =
        getProductRequestDto(Objects.requireNonNull(resourcesInUserResponseDto), user);
    multipartRequest = createMultipartRequest();
  }

  @Test
  void imageUploadSuccessfullyAndAttachToProduct() {
    ProductResponseDto productResponse = createProductWithRequest(productRequestDto);
    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponse.getId()),
            createMultipartRequest(),
            ImageResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  void downloadImageSuccessfully() {
    ProductResponseDto productResponse = createProductWithRequest(productRequestDto);
        ResponseEntity<byte[]> response =
            this.testRestTemplate.getForEntity(
                getBaseProductImageUrl(productResponse.getId()), byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void deleteImageFromFileSystem() {
    ResponseEntity<HttpStatus> response =
        this.testRestTemplate.exchange(
            getBaseProductImageUrl(UUID.randomUUID()), HttpMethod.DELETE, null, HttpStatus.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
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
  void transferProductSuccessfully() {
    ResponseEntity<ProductResponseDto> productResponse =
        this.testRestTemplate.postForEntity(
            getBaseProductUrl(), productRequestDto, ProductResponseDto.class);

    assertNotEquals(differentUser.getId(), productResponse.getBody().getOwner().getId());

    ResponseEntity<ProductResponseDto> resultResponse =
        this.testRestTemplate.exchange(
            getBaseProductUrl()
                + "/"
                + productResponse.getBody().getId()
                + "/transfer/"
                + differentUser.getId(),
            HttpMethod.PUT,
            null,
            ProductResponseDto.class);

    assertNotNull(resultResponse.getBody());
    assertEquals(differentUser.getId(), resultResponse.getBody().getOwner().getId());
    assertEquals(HttpStatus.OK, resultResponse.getStatusCode());
  }

  @Test
  void createProductSuccessfully() {

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
  void deleteProductSuccessfully() {
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
      User user, Gemstone gemstone) {
    ResourceInUserRequestDto resourceInUserRequestDto = new ResourceInUserRequestDto();
    resourceInUserRequestDto.setUserId(user.getId());
    resourceInUserRequestDto.setResourceId(gemstone.getId());
    resourceInUserRequestDto.setQuantity(20);
    return resourceInUserRequestDto;
  }

  @Nullable
  private Gemstone createGemstoneInDatabase() {
    ResourceRequestDto resourceRequest = ResourceTestHelper.getGemstoneRequestDto();
    ResponseEntity<Gemstone> createResource =
        this.testRestTemplate.postForEntity(getBaseResourceUrl(), resourceRequest, Gemstone.class);

    return createResource.getBody();
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

  private FileSystemResource createFileSystemResource() {
    return new FileSystemResource(
        new File(
            Objects.requireNonNull(getClass().getResource("/static/img/pearl.jpg")).getFile()));
  }

  private HttpEntity<MultiValueMap<String, Object>> createMultipartRequest() {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("image", createFileSystemResource());
    return new HttpEntity<>(body, headers);
  }
}
