package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.OrganizationTestHelper.getTestUserInOrganizationRequest;
import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.SystemEventTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.model.EventType.*;
import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.*;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Diamond;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ImageService;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  private Diamond diamond;
  private ResourceInOrganizationRequestDto resourceInOrganizationRequestDto;
  private ResourcesInOrganizationResponseDto resourcesInOrganizationResponseDto;
  private ProductRequestDto productRequestDto;
  private ProductResponseDto productResponseDto;
  private OrganizationResponseDto organizationResponseDto;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;
  private ProductsInOrganizationResponseDto productsInOrganizationResponseDto;

  @Autowired private ImageRepository imageRepository;
  @Autowired private ImageService imageService;

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
  }

  private String getBaseResourceInOrganizationAvailabilityUrl() {
    return buildUrl("organizations", "resources-availability");
  }

  private String getBaseProductInOrganizationUrl() {
    return buildUrl("organizations", "products");
  }

  private String getOrganizationUsersUrl(UUID organizationId) {
    return "/organizations/" + organizationId + "/users";
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

  private String getProductUrl(UUID id) {
    return "/products/" + id;
  }

  private String getBaseProductImageUrl(UUID productId) {
    return getBaseProductUrl() + "/" + productId + "/picture";
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  @BeforeEach
  void setUp() {
    organizationResponseDto = createOrganizationInDatabase(getTestOrganizationRequest());
    user = createUserInDatabase(createTestUserRequest());
    userInOrganizationRequestDto = getTestUserInOrganizationRequest(user.getId());
    addUserInOrganization(organizationResponseDto.getId(), userInOrganizationRequestDto);
    diamond = createDiamondInDatabase();
    resourceInOrganizationRequestDto =
        getResourceInOrganizationRequestDto(organizationResponseDto.getId(), diamond.getId());
    resourcesInOrganizationResponseDto =
        getResourcesInOrganizationResponseDto(resourceInOrganizationRequestDto);
    productRequestDto =
        getProductRequestDtoForOrganization(
            user,
            organizationResponseDto.getId(),
            resourcesInOrganizationResponseDto
                .getResourcesAndQuantities()
                .getFirst()
                .getResource()
                .getId(),
            getBigDecimal("20"));
    productsInOrganizationResponseDto = createProductWithRequest(productRequestDto);
    productResponseDto = productsInOrganizationResponseDto.getProducts().getFirst();
  }

  @Test
  void imageUploadShouldThrowWhenFileSizeLargerThan8MB() {
    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponseDto.getId()),
            createMultipartRequest(BIG_IMAGE_FILE),
            ImageResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void imageUploadShouldThrowWhenRequestFileIsNotImage() {
    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponseDto.getId()),
            createMultipartRequest(TEXT_FILE),
            ImageResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void imageDownloadShouldThrowWhenProductImageNotAttached() {
    ResponseEntity<byte[]> response =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(productResponseDto.getId()), byte[].class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeImageShouldThrowWhenProductImageNotAttached() {
    ResponseEntity<byte[]> response =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(productResponseDto.getId()), byte[].class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void imageUploadSuccessfullyAndAttachToProduct() {
    uploadImageAndAssertSuccessfulResponse(productResponseDto);
  }

  @Test
  void imageAttachedToProductOverrideSuccessfully() {
    uploadImageAndAssertSuccessfulResponse(productResponseDto);
    uploadImageAndAssertSuccessfulResponse(productResponseDto);
  }

  @Test
  void downloadImageSuccessfully() {
    ImageResponseDto imageResponseDto = createImageResponse(productResponseDto);
    ResponseEntity<byte[]> response =
        this.testRestTemplate.getForEntity(
            getBaseProductImageUrl(imageResponseDto.getProductId()), byte[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void deleteImageFromFileSystem() {
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
  void getProductSuccessfully() {
    ResponseEntity<ProductResponseDto> response =
        this.testRestTemplate.getForEntity(
            getProductUrl(Objects.requireNonNull(productResponseDto).getId()),
            ProductResponseDto.class);

    ProductResponseDto responseBody = response.getBody();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        productRequestDto.getResourcesContent().size(), responseBody.getResourcesContent().size());
    assertEquals(productRequestDto.getCatalogNumber(), responseBody.getCatalogNumber());
  }

  private void uploadImageAndAssertSuccessfulResponse(ProductResponseDto productResponse) {
    ResponseEntity<ImageResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductImageUrl(productResponse.getId()),
            createMultipartRequest(IMAGE_FILE),
            ImageResponseDto.class);
    ImageResponseDto imageResponseDto = response.getBody();

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(imageResponseDto);
    assertEquals(imageResponseDto.getProductId(), productResponse.getId());
  }

  private static ResourceInOrganizationRequestDto getResourceInOrganizationRequestDto(
      UUID organizationId, UUID resourceId) {
    return ResourceInOrganizationRequestDto.builder()
        .organizationId(organizationId)
        .resourceId(resourceId)
        .quantity(getBigDecimal("20"))
        .dealPrice(getBigDecimal("10"))
        .build();
  }

  @Nullable
  private ProductsInOrganizationResponseDto createProductWithRequest(
      ProductRequestDto productRequestDto) {
    ResponseEntity<ProductsInOrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseProductInOrganizationUrl(),
            productRequestDto,
            ProductsInOrganizationResponseDto.class);

    return response.getBody();
  }

  private ResourcesInOrganizationResponseDto getResourcesInOrganizationResponseDto(
      ResourceInOrganizationRequestDto resourceInOrganizationRequestDto) {
    ResponseEntity<ResourcesInOrganizationResponseDto> createResourceInOrganization =
        this.testRestTemplate.postForEntity(
            getBaseResourceInOrganizationAvailabilityUrl(),
            resourceInOrganizationRequestDto,
            ResourcesInOrganizationResponseDto.class);

    return createResourceInOrganization.getBody();
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

  private OrganizationResponseDto createOrganizationInDatabase(
      OrganizationRequestDto organizationRequestDto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), organizationRequestDto, OrganizationResponseDto.class);
    return response.getBody();
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
