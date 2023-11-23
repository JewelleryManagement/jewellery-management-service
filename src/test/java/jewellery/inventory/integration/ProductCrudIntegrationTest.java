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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
import jewellery.inventory.model.resource.PreciousStone;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ImageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class ProductCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final String IMAGE_FILE = "/static/img/pearl.jpg";
  private static final String TEXT_FILE = "/static/img/test.txt";
  private static final String BIG_IMAGE_FILE = "/static/img/Sample-jpg-image-10mb.jpg";

  @Value(value = "${image.folder.path}")
  private String PATH_TO_IMAGES;

  private User user;
  private PreciousStone preciousStone;
  private User differentUser;
  private ResourceInUserRequestDto resourceInUserRequestDto;
  private ResourcesInUserResponseDto resourcesInUserResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductRequestDto productRequestDto2;
  private ProductResponseDto productResponseDto;

  @Autowired private ImageRepository imageRepository;
  @Autowired private ImageService imageService;

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

  @BeforeEach
  void setUp() {
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
  void imageUploadShouldThrowWhenFileSizeLargerThan8MB() {
    productResponseDto = createProductWithRequest(productRequestDto);
    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponseDto.getId()),
            createMultipartRequest(BIG_IMAGE_FILE),
            ImageResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void imageUploadShouldThrowWhenRequestFileIsNotImage() {
    productResponseDto = createProductWithRequest(productRequestDto);
    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponseDto.getId()),
            createMultipartRequest(TEXT_FILE),
            ImageResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void imageDownloadShouldThrowWhenProductImageNotAttached() {
    productResponseDto = createProductWithRequest(productRequestDto);
    ResponseEntity<byte[]> response =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(productResponseDto.getId()), byte[].class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeImageShouldThrowWhenProductImageNotAttached() {
    productResponseDto = createProductWithRequest(productRequestDto);
    ResponseEntity<byte[]> response =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(productResponseDto.getId()), byte[].class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void imageUploadSuccessfullyAndAttachToProduct() {
    ProductResponseDto productResponse = createProductWithRequest(productRequestDto);
    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponse.getId()),
            createMultipartRequest(IMAGE_FILE),
            ImageResponseDto.class);
    ImageResponseDto imageResponseDto = response.getBody();

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(imageResponseDto.getProductId(), productResponse.getId());
  }

  @Test
  void downloadImageSuccessfully() {
    ProductResponseDto productResponse = createProductWithRequest(productRequestDto);
    ImageResponseDto imageResponseDto = createImageResponse(productResponse);
    ResponseEntity<byte[]> response =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(imageResponseDto.getProductId()), byte[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void deleteImageFromFileSystem() {
    productResponseDto = createProductWithRequest(productRequestDto);
    ImageResponseDto imageResponseDto = createImageResponse(productResponseDto);
    ResponseEntity<HttpStatus> response =
        this.testRestTemplate.exchange(
            getBaseProductImageUrl(imageResponseDto.getProductId()),
            HttpMethod.DELETE,
            null,
            HttpStatus.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    ResponseEntity<byte[]> responseEntity =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(productResponseDto.getId()), byte[].class);

    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
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

    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(productResponse2.getBody(), resultResponse.getBody(), objectMapper);

    assertEventWasLogged(
        this.testRestTemplate,
        objectMapper,
        getBaseSystemEventUrl(),
        PRODUCT_TRANSFER,
        expectedEventPayload);
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

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(productResponseDto, objectMapper);

    assertEventWasLogged(
        this.testRestTemplate,
        objectMapper,
        getBaseSystemEventUrl(),
        PRODUCT_CREATE,
        expectedEventPayload);
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
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(productResponseDto, objectMapper);

    assertEventWasLogged(
        this.testRestTemplate,
        objectMapper,
        getBaseSystemEventUrl(),
        PRODUCT_DISASSEMBLY,
        expectedEventPayload);
  }

  @Test
  void deleteProductWithAttachedPicture() {
    productResponseDto = createProductWithRequest(productRequestDto);
    ImageResponseDto imageResponseDto = createImageResponse(productResponseDto);

    assertEquals(imageResponseDto.getProductId(), productResponseDto.getId());
    assertTrue(Files.exists(Path.of(PATH_TO_IMAGES + productResponseDto.getId().toString())));

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

    ResponseEntity<byte[]> byteResponse =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(productResponseDto.getId()), byte[].class);

    assertFalse(Files.exists(Path.of(PATH_TO_IMAGES + productResponseDto.getId().toString())));
    assertEquals(HttpStatus.NOT_FOUND, byteResponse.getStatusCode());
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

  private FileSystemResource createFileSystemResource(String path) {
    return new FileSystemResource(
        new File(Objects.requireNonNull(getClass().getResource(path)).getFile()));
  }

  private HttpEntity<MultiValueMap<String, Object>> createMultipartRequest(String path) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("image", createFileSystemResource(path));
    return new HttpEntity<>(body, headers);
  }

  private ImageResponseDto createImageResponse(ProductResponseDto productResponseDto) {

    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponseDto.getId()),
            createMultipartRequest(IMAGE_FILE),
            ImageResponseDto.class);

    return response.getBody();
  }
}
