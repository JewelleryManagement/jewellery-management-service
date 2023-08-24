package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.getGemstoneRequestDto;
import static jewellery.inventory.helper.UserTestHelper.createResourceInUserRequestDto;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.ResourceQuantityDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ResourceInUserCrudIntegrationTest {
  @Autowired TestRestTemplate testRestTemplate;
  @Autowired UserRepository userRepository;
  @Autowired ResourceRepository resourceRepository;
  @Autowired ResourceInUserRepository resourceInUserRepository;

  @Value(value = "${local.server.port}")
  private int port;

  private static final String BASE_URL_PATH = "http://localhost:";

  private String getBaseUrl() {
    return BASE_URL_PATH + port;
  }

  private String buildUrl(String... paths) {
    return getBaseUrl() + "/" + String.join("/", paths);
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("resources", "availability");
  }

  private String getResourceAvailabilityUrl(UUID userId, UUID resourceId) {
    return buildUrl("resources", "availability", userId.toString(), resourceId.toString());
  }

  private String getBaseUserUrl() {
    return buildUrl("users");
  }

  private String getBaseResourceUrl() {
    return buildUrl("resources");
  }

  private UUID existingUserId;
  private UUID existingResourceId;
  private static final double RESOURCE_QUANTITY = 5.00;

  @BeforeEach
  void setup() {
    existingUserId = UUID.randomUUID();
    existingResourceId = UUID.randomUUID();
  }

  @AfterEach
  void cleanup() {
    userRepository.deleteAll();
    resourceRepository.deleteAll();
    resourceInUserRepository.deleteAll();
  }

  @Test
  void addResourceToUserSuccessfully() {
    UserResponseDto createdUser = getUserResponseDto();
    GemstoneResponseDto createdResource = getGemstoneResponseDto();

    ResourceInUserRequestDto requestDto =
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY);

    String url = getBaseResourceAvailabilityUrl();
    ResponseEntity<ResourcesInUserResponseDto> response =
        testRestTemplate.postForEntity(url, requestDto, ResourcesInUserResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(createdUser.getId(), response.getBody().getOwner().getId());
  }

  @Test
  void addResourceToUserFailsWhenUserNotFound() {
    ResourceInUserRequestDto requestDto =
        createResourceInUserRequestDto(existingUserId, existingResourceId, RESOURCE_QUANTITY);
    ResponseEntity<UserResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, UserResponseDto.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToUserFailsWhenResourceNotFound() {
    ResourceInUserRequestDto requestDto =
        createResourceInUserRequestDto(existingUserId, existingResourceId, RESOURCE_QUANTITY);

    ResponseEntity<UserResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, UserResponseDto.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToUserFailsWhenQuantityInvalid() {
    ResourceInUserRequestDto requestDto =
        createResourceInUserRequestDto(existingUserId, existingResourceId, -5);

    ResponseEntity<UserResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, UserResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void updateResourceInUserSuccessfullyWhenAddedMultipleTimes() {
    UserResponseDto createdUser = getUserResponseDto();
    GemstoneResponseDto createdResource = getGemstoneResponseDto();

    ResourceInUserRequestDto requestDto =
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY);

    ResponseEntity<ResourcesInUserResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, ResourcesInUserResponseDto.class);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    ResponseEntity<ResourcesInUserResponseDto> response2 =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, ResourcesInUserResponseDto.class);
    assertEquals(HttpStatus.CREATED, response2.getStatusCode());

    ResourcesInUserResponseDto userResponseDto2 = response2.getBody();
    assertNotNull(userResponseDto2);

    List<ResourceQuantityDto> resourceQuantities = userResponseDto2.getResourcesAndQuantities();
    assertNotNull(resourceQuantities);
    assertFalse(resourceQuantities.isEmpty());

    double totalQuantity =
        resourceQuantities.stream().mapToDouble(ResourceQuantityDto::getQuantity).sum();

    assertEquals(10.00, totalQuantity, 0.001);
  }

  @Test
  void getAllResourcesFromUserSuccessfully() {
    UserResponseDto createdUser = getUserResponseDto();
    GemstoneResponseDto createdResource1 = getGemstoneResponseDto();
    GemstoneResponseDto createdResource2 = getGemstoneResponseDto();

    ResourceInUserRequestDto requestDto1 =
        createResourceInUserRequestDto(createdUser.getId(), createdResource1.getId(), 5.00);
    testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(), requestDto1, UserResponseDto.class);

    ResourceInUserRequestDto requestDto2 =
        createResourceInUserRequestDto(createdUser.getId(), createdResource2.getId(), 3.00);
    testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(), requestDto2, UserResponseDto.class);

    String url = getBaseResourceAvailabilityUrl() + "/" + createdUser.getId();

    ResponseEntity<ResourcesInUserResponseDto> response =
        this.testRestTemplate.getForEntity(url, ResourcesInUserResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getResourcesAndQuantities().size());
  }

  @Test
  void getAllResourcesFromUserReturnsEmptyWhenUserHasNoResources() {
    UserResponseDto createdUser = getUserResponseDto();

    String url = getBaseResourceAvailabilityUrl() + "/" + createdUser.getId();
    ResponseEntity<ResourcesInUserResponseDto> response =
        this.testRestTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getResourcesAndQuantities().isEmpty());
  }

  @Test
  void removeResourceFromUserSuccessfully() {
    UserResponseDto createdUser = getUserResponseDto();
    GemstoneResponseDto createdResource = getGemstoneResponseDto();
    addResourceToUser(createdUser, createdResource);

    removeResourceFromUser(createdUser, createdResource);

    ResourcesInUserResponseDto userResources = getUserResources(createdUser);

    boolean resourceStillExists =
        userResources.getResourcesAndQuantities().stream()
            .anyMatch(
                resourceQuantity ->
                    resourceQuantity.getResource().getId().equals(createdResource.getId()));
    assertFalse(resourceStillExists);
  }

  @Test
  public void removeResourceFromUserFailsWhenResourceNotFound() {
    UserResponseDto createdUser = getUserResponseDto();
    UUID nonExistentResourceId = UUID.randomUUID();

    String url =
        getBaseResourceAvailabilityUrl() + "/" + createdUser.getId() + "/" + nonExistentResourceId;

    ResponseEntity<String> response =
        testRestTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void removeResourceFromUserFailsWhenUserNotFound() {
    UUID nonExistentUserId = UUID.randomUUID();
    GemstoneResponseDto createdResource = getGemstoneResponseDto();
    String url = getResourceAvailabilityUrl(nonExistentUserId, createdResource.getId());

    ResponseEntity<String> response =
        testRestTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserSuccessfully() {
    UserResponseDto createdUser = getUserResponseDto();
    assertNotNull(createdUser);

    GemstoneResponseDto createdResource = getGemstoneResponseDto();
    assertNotNull(createdResource);
    addResourceToUser(createdUser, createdResource);

    String url = getResourceAvailabilityUrl(createdUser.getId(), createdResource.getId()) + "/" + 1;

    testRestTemplate.delete(url);
    ResourcesInUserResponseDto userResources = getUserResources(createdUser);

    // Extract the specific resource whose quantity we decreased
    ResourceQuantityDto resourceQuantity =
        userResources.getResourcesAndQuantities().stream()
            .filter(rq -> rq.getResource().getId().equals(createdResource.getId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected resource not found"));

    // Assert that the resource's quantity is decremented
    assertEquals(RESOURCE_QUANTITY - 1, resourceQuantity.getQuantity(), 0.01);
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenResourceNotFound() {
    UserResponseDto createdUser = getUserResponseDto();
    assertNotNull(createdUser);

    UUID nonExistentResourceId = UUID.randomUUID();
    String url = getResourceAvailabilityUrl(createdUser.getId(), nonExistentResourceId) + "/" + 1;

    ResponseEntity<String> response =
        testRestTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenUserNotFound() {
    UUID nonExistentUserId = UUID.randomUUID();
    GemstoneResponseDto createdResource = getGemstoneResponseDto();
    assertNotNull(createdResource);

    String url = getResourceAvailabilityUrl(nonExistentUserId, createdResource.getId()) + "/" + 1;

    ResponseEntity<String> response =
        testRestTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenInsufficientQuantity() {
    UserResponseDto createdUser = getUserResponseDto();
    assertNotNull(createdUser);

    GemstoneResponseDto createdResource = getGemstoneResponseDto();
    assertNotNull(createdResource);
    addResourceToUser(createdUser, createdResource);

    String url =
        getResourceAvailabilityUrl(createdUser.getId(), createdResource.getId())
            + "/"
            + (RESOURCE_QUANTITY + 1);

    ResponseEntity<String> response =
        testRestTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenQuantityNegative() {
    UserResponseDto createdUser = getUserResponseDto();
    assertNotNull(createdUser);

    GemstoneResponseDto createdResource = getGemstoneResponseDto();
    assertNotNull(createdResource);
    addResourceToUser(createdUser, createdResource);

    String url =
        getResourceAvailabilityUrl(createdUser.getId(), createdResource.getId()) + "/" + -1;

    ResponseEntity<String> response =
        testRestTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  private GemstoneResponseDto getGemstoneResponseDto() {
    ResourceRequestDto resourceRequest = getGemstoneRequestDto();
    ResponseEntity<GemstoneResponseDto> resourceResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, GemstoneResponseDto.class);

    assertEquals(HttpStatus.CREATED, resourceResponseEntity.getStatusCode());

    GemstoneResponseDto createdResource = resourceResponseEntity.getBody();
    assertNotNull(createdResource);
    assertNotNull(createdResource.getId());
    return createdResource;
  }

  private UserResponseDto getUserResponseDto() {
    UserRequestDto userRequest = createTestUserRequest();
    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    UserResponseDto createdUser = userResponseEntity.getBody();
    assertNotNull(createdUser);
    return createdUser;
  }

  private void addResourceToUser(UserResponseDto user, GemstoneResponseDto resource) {
    ResourceInUserRequestDto requestDto =
        createResourceInUserRequestDto(user.getId(), resource.getId(), RESOURCE_QUANTITY);
    ResponseEntity<UserResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, UserResponseDto.class);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  private void removeResourceFromUser(UserResponseDto user, GemstoneResponseDto resource) {
    String removeResourceUrl =
        getBaseResourceAvailabilityUrl() + "/" + user.getId() + "/" + resource.getId();
    testRestTemplate.delete(removeResourceUrl);
  }

  private ResourcesInUserResponseDto getUserResources(UserResponseDto user) {
    String getUserResourcesUrl = getBaseResourceAvailabilityUrl() + "/" + user.getId();
    ResponseEntity<ResourcesInUserResponseDto> getUserResourcesResponse =
        testRestTemplate.exchange(
            getUserResourcesUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    return getUserResourcesResponse.getBody();
  }
}
