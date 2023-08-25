package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.getGemstoneRequestDto;
import static jewellery.inventory.helper.UserTestHelper.createResourceInUserRequestDto;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.ResourceQuantityDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ResourceInUserCrudIntegrationTest {
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

  private static final double RESOURCE_QUANTITY = 5.00;

  @AfterEach
  void cleanup() {
    userRepository.deleteAll();
    resourceRepository.deleteAll();
    resourceInUserRepository.deleteAll();
  }

  @Test
  void addResourceToUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourceInUserRequestDto(
                createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getResourcesAndQuantities().size());
    assertEquals(createdUser, response.getBody().getOwner());
  }

  @Test
  void addResourceToUserFailsWhenUserNotFound() {
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();
    UUID nonExistentUserId = UUID.randomUUID();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourceInUserRequestDto(
                nonExistentUserId, createdResource.getId(), RESOURCE_QUANTITY));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToUserFailsWhenResourceNotFound() {
    UserResponseDto createdUser = sendCreateUserRequest();
    UUID nonExistentResourceId = UUID.randomUUID();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourceInUserRequestDto(
                createdUser.getId(), nonExistentResourceId, RESOURCE_QUANTITY));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToUserFailsWhenQuantityInvalid() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourceInUserRequestDto(createdUser.getId(), createdResource.getId(), -5));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void updateResourceInUserSuccessfullyWhenAddedMultipleTimes() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();
    ResourceInUserRequestDto resourceInUserRequestDto =
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY);

    sendAddResourceInUserRequest(resourceInUserRequestDto);
    sendAddResourceInUserRequest(resourceInUserRequestDto);

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendGetResourcesInUserRequest(createdUser.getId());
    assertNotNull(response.getBody());
    List<ResourceQuantityDto> resourceQuantities = response.getBody().getResourcesAndQuantities();
    assertNotNull(resourceQuantities);
    assertEquals(1, resourceQuantities.size());
    assertEquals(RESOURCE_QUANTITY * 2, resourceQuantities.get(0).getQuantity(), 0.001);
  }

  @Test
  void getAllResourcesFromUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto firstCreatedResource = sendCreateGemstoneRequest();
    GemstoneResponseDto secondCreatedResource = sendCreateGemstoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(createdUser.getId(), firstCreatedResource.getId(), 5.00));
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(createdUser.getId(), secondCreatedResource.getId(), 3.00));

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendGetResourcesInUserRequest(createdUser.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getResourcesAndQuantities().size());
    assertCreatedResourceIsInResourcesInUser(firstCreatedResource, response);
    assertCreatedResourceIsInResourcesInUser(secondCreatedResource, response);
  }

  @Test
  void getAllResourcesFromUserReturnsEmptyWhenUserHasNoResources() {
    UserResponseDto createdUser = sendCreateUserRequest();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendGetResourcesInUserRequest(createdUser.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getResourcesAndQuantities().isEmpty());
  }

  @Test
  void removeResourceFromUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    sendDeleteResourceInUserRequest(createdUser.getId(), createdResource.getId());

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendGetResourcesInUserRequest(createdUser.getId());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getResourcesAndQuantities().isEmpty());
  }

  @Test
  void removeResourceFromUserFailsWhenResourceNotFound() {
    UserResponseDto createdUser = sendCreateUserRequest();
    UUID nonExistentResourceId = UUID.randomUUID();

    ResponseEntity<String> response =
        sendDeleteResourceInUserRequest(createdUser.getId(), nonExistentResourceId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceFromUserFailsWhenUserNotFound() {
    UUID nonExistentUserId = UUID.randomUUID();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();

    ResponseEntity<String> response =
        sendDeleteResourceInUserRequest(nonExistentUserId, createdResource.getId());

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    sendDeleteQuantityFromResourceInUserRequest(createdUser.getId(), createdResource.getId(), 1.0);

    ResponseEntity<ResourcesInUserResponseDto> resourcesInUserResponse =
        sendGetResourcesInUserRequest(createdUser.getId());
    ResourceQuantityDto resourceQuantity =
        findResourceQuantityIn(createdResource.getId(), resourcesInUserResponse);
    assertEquals(RESOURCE_QUANTITY - 1, resourceQuantity.getQuantity(), 0.01);
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenResourceNotFound() {
    UserResponseDto createdUser = sendCreateUserRequest();
    UUID nonExistentResourceId = UUID.randomUUID();

    ResponseEntity<String> response =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(), nonExistentResourceId, 1.0);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenUserNotFound() {
    UUID nonExistentUserId = UUID.randomUUID();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();

    ResponseEntity<String> response =
        sendDeleteQuantityFromResourceInUserRequest(
            nonExistentUserId, createdResource.getId(), 1.8);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenInsufficientQuantity() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    ResponseEntity<String> response =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY + 1);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenQuantityNegative() {
    UserResponseDto createdUser = sendCreateUserRequest();
    GemstoneResponseDto createdResource = sendCreateGemstoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    ResponseEntity<String> response =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(), createdResource.getId(), -1.0);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  private GemstoneResponseDto sendCreateGemstoneRequest() {
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

  private UserResponseDto sendCreateUserRequest() {
    UserRequestDto userRequest = createTestUserRequest();
    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    UserResponseDto createdUser = userResponseEntity.getBody();
    assertNotNull(createdUser);
    return createdUser;
  }

  private ResponseEntity<String> sendDeleteResourceInUserRequest(UUID userId, UUID resourceId) {
    String removeResourceUrl = getBaseResourceAvailabilityUrl() + "/" + userId + "/" + resourceId;
    return testRestTemplate.exchange(
        removeResourceUrl, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
  }

  private ResponseEntity<String> sendDeleteQuantityFromResourceInUserRequest(
      UUID userId, UUID resourceId, double quantity) {
    String removeResourceUrl =
        getBaseResourceAvailabilityUrl() + "/" + userId + "/" + resourceId + "/" + quantity;
    return testRestTemplate.exchange(
        removeResourceUrl, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
  }

  private ResponseEntity<ResourcesInUserResponseDto> sendAddResourceInUserRequest(
      ResourceInUserRequestDto resourceInUserRequestDto) {
    return testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(),
        resourceInUserRequestDto,
        ResourcesInUserResponseDto.class);
  }

  private ResponseEntity<ResourcesInUserResponseDto> sendGetResourcesInUserRequest(UUID userId) {
    return testRestTemplate.getForEntity(
        getBaseResourceAvailabilityUrl() + "/" + userId, ResourcesInUserResponseDto.class);
  }

  private static void assertCreatedResourceIsInResourcesInUser(
      GemstoneResponseDto firstCreatedResource,
      ResponseEntity<ResourcesInUserResponseDto> response) {
    assertNotNull(response.getBody());
    assertTrue(
        response.getBody().getResourcesAndQuantities().stream()
            .anyMatch(
                resourceQuantityDto ->
                    resourceQuantityDto.getResource().equals(firstCreatedResource)));
  }

  private static ResourceQuantityDto findResourceQuantityIn(
      UUID idToFind, ResponseEntity<ResourcesInUserResponseDto> resourcesInUserResponse) {
    assertNotNull(resourcesInUserResponse.getBody());
    return resourcesInUserResponse.getBody().getResourcesAndQuantities().stream()
        .filter(rq -> rq.getResource().getId().equals(idToFind))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected resource not found"));
  }
}