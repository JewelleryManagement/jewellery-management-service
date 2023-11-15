package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.getPreciousStoneRequestDto;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.PreciousStoneResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.repository.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

class ResourceInUserCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final double RESOURCE_QUANTITY = 5.00;
  @Autowired UserRepository userRepository;
  @Autowired SaleRepository saleRepository;
  @Autowired
  ProductRepository productRepository;
  @Autowired ResourceRepository resourceRepository;
  @Autowired ResourceInUserRepository resourceInUserRepository;

  private String getBaseUrl() {
    return BASE_URL_PATH + port;
  }

  private String buildUrl(String... paths) {
    return getBaseUrl() + "/" + String.join("/", paths);
  }

  private String getBaseResourceAvailabilityUrl() {
    return buildUrl("resources", "availability");
  }

  private String getBaseResourceAvailabilityTransferUrl() {
    return buildUrl("resources", "availability", "transfer");
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

  @BeforeEach
  void cleanup() {
    productRepository.deleteAll();
    saleRepository.deleteAll();
    userRepository.deleteAll();
    resourceRepository.deleteAll();
    resourceInUserRepository.deleteAll();
  }

  @Test
  void addResourceToUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

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
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
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
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourceInUserRequestDto(createdUser.getId(), createdResource.getId(), -5));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void updateResourceInUserSuccessfullyWhenAddedMultipleTimes() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    ResourceInUserRequestDto resourceInUserRequestDto =
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY);

    sendAddResourceInUserRequest(resourceInUserRequestDto);
    sendAddResourceInUserRequest(resourceInUserRequestDto);

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendGetResourcesInUserRequest(createdUser.getId());
    assertNotNull(response.getBody());
    List<ResourceQuantityResponseDto> resourceQuantities =
        response.getBody().getResourcesAndQuantities();
    assertNotNull(resourceQuantities);
    assertEquals(1, resourceQuantities.size());
    assertEquals(RESOURCE_QUANTITY * 2, resourceQuantities.get(0).getQuantity(), 0.001);
  }

  @Test
  void getAllResourcesFromUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto firstCreatedResource = sendCreatePreciousStoneRequest();
    PreciousStoneResponseDto secondCreatedResource = sendCreatePreciousStoneRequest();
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
  void getAllUsersOwningResourceSuccessfully() {
    UserResponseDto firstCreatedUser = sendCreateUserRequest(createTestUserRequest());
    UserResponseDto secondCreatedUser = sendCreateUserRequest(createDifferentUserRequest());
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(firstCreatedUser.getId(), createdResource.getId(), 5.00));
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(secondCreatedUser.getId(), createdResource.getId(), 3.00));

    ResponseEntity<ResourceOwnedByUsersResponseDto> response =
        sendGetUsersAndQuantitiesForResourceRequest(createdResource.getId());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getUsersAndQuantities().size());
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
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
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
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

    ResponseEntity<String> response =
        sendDeleteResourceInUserRequest(nonExistentUserId, createdResource.getId());

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    sendDeleteQuantityFromResourceInUserRequest(createdUser.getId(), createdResource.getId(), 1.0);

    ResponseEntity<ResourcesInUserResponseDto> resourcesInUserResponse =
        sendGetResourcesInUserRequest(createdUser.getId());
    ResourceQuantityResponseDto resourceQuantity =
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
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

    ResponseEntity<String> response =
        sendDeleteQuantityFromResourceInUserRequest(
            nonExistentUserId, createdResource.getId(), 1.8);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenInsufficientQuantity() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
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
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(
            createdUser.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    ResponseEntity<String> response =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(), createdResource.getId(), -1.0);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void transferResourceFromUserToAnotherUserSuccessfully() {
    UserResponseDto sender = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourceInUserRequestDto(sender.getId(), createdResource.getId(), RESOURCE_QUANTITY));

    UserResponseDto receiver = sendCreateUserRequest(createDifferentUserRequest());

    TransferResourceRequestDto requestDto =
        getTransferResourceRequestDto(sender, createdResource, receiver);

    ResponseEntity<TransferResourceResponseDto> transferResourceResponseDtoResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseResourceAvailabilityTransferUrl(),
            requestDto,
            TransferResourceResponseDto.class);
    TransferResourceResponseDto response = transferResourceResponseDtoResponseEntity.getBody();

    assertEquals(HttpStatus.OK, transferResourceResponseDtoResponseEntity.getStatusCode());

    assertEquals(
        Objects.requireNonNull(response).getPreviousOwner().getId(),
        requestDto.getPreviousOwnerId());
    assertEquals(
        Objects.requireNonNull(response).getNewOwner().getId(), requestDto.getNewOwnerId());
    assertEquals(
        response.getTransferredResource().getResource().getId(),
        requestDto.getTransferredResourceId());
    assertEquals(response.getTransferredResource().getQuantity(), 1);
  }

  @NotNull
  private static TransferResourceRequestDto getTransferResourceRequestDto(
      UserResponseDto sender, PreciousStoneResponseDto createdResource, UserResponseDto receiver) {
    TransferResourceRequestDto requestDto = new TransferResourceRequestDto();
    requestDto.setPreviousOwnerId(sender.getId());
    requestDto.setNewOwnerId(receiver.getId());
    requestDto.setTransferredResourceId(createdResource.getId());
    requestDto.setQuantity(1);
    return requestDto;
  }

  private PreciousStoneResponseDto sendCreatePreciousStoneRequest() {
    ResourceRequestDto resourceRequest = getPreciousStoneRequestDto();
    ResponseEntity<PreciousStoneResponseDto> resourceResponseEntity =
        this.testRestTemplate.postForEntity(
            getBaseResourceUrl(), resourceRequest, PreciousStoneResponseDto.class);

    assertEquals(HttpStatus.CREATED, resourceResponseEntity.getStatusCode());

    PreciousStoneResponseDto createdResource = resourceResponseEntity.getBody();
    assertNotNull(createdResource);
    assertNotNull(createdResource.getId());
    return createdResource;
  }

  private UserResponseDto sendCreateUserRequest(UserRequestDto userRequest) {
    ResponseEntity<UserResponseDto> userResponseEntity =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, UserResponseDto.class);
    UserResponseDto createdUser = userResponseEntity.getBody();
    assertNotNull(createdUser);
    return createdUser;
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

  private ResponseEntity<ResourceOwnedByUsersResponseDto>
      sendGetUsersAndQuantitiesForResourceRequest(UUID resourceId) {
    return testRestTemplate.getForEntity(
        getBaseResourceAvailabilityUrl() + "/by-resource/" + resourceId,
        ResourceOwnedByUsersResponseDto.class);
  }

  private static void assertCreatedResourceIsInResourcesInUser(
      PreciousStoneResponseDto firstCreatedResource,
      ResponseEntity<ResourcesInUserResponseDto> response) {
    assertNotNull(response.getBody());
    assertTrue(
        response.getBody().getResourcesAndQuantities().stream()
            .anyMatch(
                resourceQuantityDto ->
                    resourceQuantityDto.getResource().equals(firstCreatedResource)));
  }

  private static ResourceQuantityResponseDto findResourceQuantityIn(
      UUID idToFind, ResponseEntity<ResourcesInUserResponseDto> resourcesInUserResponse) {
    assertNotNull(resourcesInUserResponse.getBody());
    return resourcesInUserResponse.getBody().getResourcesAndQuantities().stream()
        .filter(rq -> rq.getResource().getId().equals(idToFind))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected resource not found"));
  }
}
