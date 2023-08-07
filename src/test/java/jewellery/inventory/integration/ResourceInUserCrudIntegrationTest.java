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
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.dto.response.resource.ResourceInUserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
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

  private String getBaseResourceAvailabilityUrl() {
    return "http://localhost:" + port + "/resources/availability";
  }

  private String getResourceAvailabilityUrl(UUID userId, UUID resourceId) {
    return "http://localhost:" + port + "/resources/availability/" + userId + "/" + resourceId;
  }

  private String getBaseUserUrl() {
    return "http://localhost:" + port + "/users";
  }

  private String getBaseResourceUrl() {
    return "http://localhost:" + port + "/resources";
  }

  private UUID existingUserId;
  private UUID existingResourceId;
  private static final double RESOURCE_QUANTITY = 5.00;

  @BeforeEach
  void setup() {
    existingUserId = UUID.randomUUID();
    existingResourceId = UUID.randomUUID();
    testRestTemplate = new TestRestTemplate();
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

    System.out.println(createdUser.getId());
    System.out.println(createdResource.getId());
    String url = getBaseResourceAvailabilityUrl();
    ResponseEntity<ResourceInUserResponseDto> response =
        testRestTemplate.postForEntity(url, requestDto, ResourceInUserResponseDto.class);

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

    ResponseEntity<ResourceInUserResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, ResourceInUserResponseDto.class);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    ResponseEntity<ResourceInUserResponseDto> response2 =
        testRestTemplate.postForEntity(
            getBaseResourceAvailabilityUrl(), requestDto, ResourceInUserResponseDto.class);
    assertEquals(HttpStatus.CREATED, response2.getStatusCode());

    ResourceInUserResponseDto userResponseDto2 = response2.getBody();
    assertNotNull(userResponseDto2);

    List<ResourceResponseDto> resources = response2.getBody().getResources();
    assertNotNull(resources);
    assertFalse(resources.isEmpty());

    assertEquals(10.00, response2.getBody().getQuantity(), 0.001);
  }

  @Test
  void getAllResourcesFromUserSuccessfully() {
    UserResponseDto createdUser = getUserResponseDto();
    GemstoneResponseDto createdResource1 = getGemstoneResponseDto();
    GemstoneResponseDto createdResource2 = getGemstoneResponseDto();
    String url = getBaseResourceAvailabilityUrl() + "/" + createdUser.getId();
    ResourceInUserRequestDto requestDto1 =
        createResourceInUserRequestDto(createdUser.getId(), createdResource1.getId(), 5.00);
    testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(), requestDto1, UserResponseDto.class);

    ResourceInUserRequestDto requestDto2 =
        createResourceInUserRequestDto(createdUser.getId(), createdResource2.getId(), 3.00);
    testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(), requestDto2, UserResponseDto.class);

    ResponseEntity<List<ResourceInUserResponseDto>> response =
        this.testRestTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());
  }

  @Test
  void getAllResourcesFromUserReturnsEmptyWhenUserHasNoResources() {
    UserResponseDto createdUser = getUserResponseDto();

    String url = getBaseResourceAvailabilityUrl() + "/" + createdUser.getId();
    ResponseEntity<List<ResourceInUserResponseDto>> response =
        this.testRestTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void removeResourceFromUserSuccessfully() {
    UserResponseDto createdUser = getUserResponseDto();
    GemstoneResponseDto createdResource = getGemstoneResponseDto();
    addResourceToUser(createdUser, createdResource);

    removeResourceFromUser(createdUser, createdResource);

    List<ResourceInUserResponseDto> userResources = getUserResources(createdUser);
    assertFalse(
        userResources.stream()
            .flatMap(resourceInUser -> resourceInUser.getResources().stream())
            .anyMatch(resource -> resource.getId().equals(createdResource.getId())));
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
    List<ResourceInUserResponseDto> userResources = getUserResources(createdUser);

    ResourceInUserResponseDto resourceInUser =
        userResources.stream()
            .filter(
                resourceUserDto ->
                    resourceUserDto.getResources().stream()
                        .anyMatch(
                            resourceDto -> resourceDto.getId().equals(createdResource.getId())))
            .findFirst()
            .orElseThrow();

    assertEquals(RESOURCE_QUANTITY - 1, resourceInUser.getQuantity(), 0.01);
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

  private List<ResourceInUserResponseDto> getUserResources(UserResponseDto user) {
    String getUserResourcesUrl = getBaseResourceAvailabilityUrl() + "/" + user.getId();
    ResponseEntity<List<ResourceInUserResponseDto>> getUserResourcesResponse =
        testRestTemplate.exchange(
            getUserResourcesUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    return getUserResourcesResponse.getBody();
  }
}
