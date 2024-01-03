package jewellery.inventory.integration;

import static jewellery.inventory.helper.ResourceTestHelper.getPreciousStoneRequestDto;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.helper.UserTestHelper.*;
import static jewellery.inventory.model.EventType.RESOURCE_ADD_QUANTITY;
import static jewellery.inventory.model.EventType.RESOURCE_REMOVE_QUANTITY;
import static jewellery.inventory.model.EventType.RESOURCE_TRANSFER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInUserRequestDto;
import jewellery.inventory.dto.request.ResourcePurchaseRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.PreciousStoneResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

class ResourceInUserCrudIntegrationTest extends AuthenticatedIntegrationTestBase {

  private static final BigDecimal RESOURCE_QUANTITY =
      BigDecimal.valueOf(5).setScale(2, RoundingMode.HALF_UP);

  private String buildUrl(String... paths) {
    return "/" + String.join("/", paths);
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

  @Test
  void addResourceToUserSuccessfully() throws JsonProcessingException {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

    ResponseEntity<ResourcePurchaseResponseDto> response =
        sendPurchaseResourceRequest(
            createResourcePurchaseRequest(
                createdUser.getId(),
                createdResource.getId(),
                RESOURCE_QUANTITY,
                BigDecimal.valueOf(555.55)));

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getResourcesAndQuantities().size());
    assertEquals(createdUser, response.getBody().getOwner());
    ResourcePurchaseResponseDto result = response.getBody();

    Map<String, Object> expectedEventPayload = getUpdateEventPayload(null, result, objectMapper);

    systemEventTestHelper.assertEventWasLogged(RESOURCE_ADD_QUANTITY, expectedEventPayload);
  }

  @Test
  void addResourceToUserFailsWhenUserNotFound() {
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    UUID nonExistentUserId = UUID.randomUUID();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourcePurchaseRequestDto(
                nonExistentUserId,
                createdResource.getId(),
                RESOURCE_QUANTITY,
                BigDecimal.valueOf(105.5)));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToUserFailsWhenResourceNotFound() {
    UserResponseDto createdUser = sendCreateUserRequest();
    UUID nonExistentResourceId = UUID.randomUUID();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourcePurchaseRequestDto(
                createdUser.getId(),
                nonExistentResourceId,
                RESOURCE_QUANTITY,
                BigDecimal.valueOf(105.5)));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void addResourceToUserFailsWhenQuantityInvalid() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourcePurchaseRequestDto(
                createdUser.getId(),
                createdResource.getId(),
                BigDecimal.valueOf(-5),
                BigDecimal.valueOf(-105.5)));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void updateResourceInUserSuccessfullyWhenAddedMultipleTimes() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    ResourceInUserRequestDto resourceInUserRequestDto =
        createResourcePurchaseRequestDto(
            createdUser.getId(),
            createdResource.getId(),
            RESOURCE_QUANTITY,
            BigDecimal.valueOf(105.5));

    sendAddResourceInUserRequest(resourceInUserRequestDto);
    sendAddResourceInUserRequest(resourceInUserRequestDto);

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendGetResourcesInUserRequest(createdUser.getId());
    assertNotNull(response.getBody());
    List<ResourceQuantityResponseDto> resourceQuantities =
        response.getBody().getResourcesAndQuantities();
    assertNotNull(resourceQuantities);
    assertEquals(1, resourceQuantities.size());
    assertEquals(
        RESOURCE_QUANTITY.multiply(BigDecimal.valueOf(2)),
        resourceQuantities.get(0).getQuantity(),
        String.valueOf(0.01));
  }

  @Test
  void getAllResourcesFromUserSuccessfully() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto firstCreatedResource = sendCreatePreciousStoneRequest();
    PreciousStoneResponseDto secondCreatedResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourcePurchaseRequestDto(
            createdUser.getId(),
            firstCreatedResource.getId(),
            BigDecimal.valueOf(5.00),
            BigDecimal.valueOf(105.5)));
    sendAddResourceInUserRequest(
        createResourcePurchaseRequestDto(
            createdUser.getId(),
            secondCreatedResource.getId(),
            BigDecimal.valueOf(3.00),
            BigDecimal.valueOf(105.5)));

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
        createResourcePurchaseRequestDto(
            firstCreatedUser.getId(),
            createdResource.getId(),
            BigDecimal.valueOf(5.00),
            BigDecimal.valueOf(105.5)));
    sendAddResourceInUserRequest(
        createResourcePurchaseRequestDto(
            secondCreatedUser.getId(),
            createdResource.getId(),
            BigDecimal.valueOf(3.00),
            BigDecimal.valueOf(105.5)));

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
  void removeResourceFromUserSuccessfully() throws JsonProcessingException {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

    ResponseEntity<ResourcesInUserResponseDto> entity =
        sendAddResourceInUserRequest(
            createResourcePurchaseRequestDto(
                createdUser.getId(),
                createdResource.getId(),
                RESOURCE_QUANTITY,
                BigDecimal.valueOf(105.5)));

    sendDeleteResourceInUserRequest(createdUser.getId(), createdResource.getId());

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendGetResourcesInUserRequest(createdUser.getId());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getResourcesAndQuantities().isEmpty());

    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(entity.getBody(), null, objectMapper);

    systemEventTestHelper.assertEventWasLogged(RESOURCE_REMOVE_QUANTITY, expectedEventPayload);
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
  void removeResourceQuantityFromUserSuccessfully() throws JsonProcessingException {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    ResponseEntity<ResourcesInUserResponseDto> response =
        sendAddResourceInUserRequest(
            createResourcePurchaseRequestDto(
                createdUser.getId(),
                createdResource.getId(),
                RESOURCE_QUANTITY,
                BigDecimal.valueOf(105.5)));

    ResponseEntity<ResourcesInUserResponseDto> deleteQuantityResponse =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(), createdResource.getId(), BigDecimal.valueOf(1.0));

    ResponseEntity<ResourcesInUserResponseDto> resourcesInUserResponse =
        sendGetResourcesInUserRequest(createdUser.getId());
    ResourceQuantityResponseDto resourceQuantity =
        findResourceQuantityIn(createdResource.getId(), resourcesInUserResponse);
    assertEquals(
        RESOURCE_QUANTITY.subtract(BigDecimal.valueOf(1).setScale(1, RoundingMode.HALF_UP)),
        resourceQuantity.getQuantity());

    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(response.getBody(), deleteQuantityResponse.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(RESOURCE_REMOVE_QUANTITY, expectedEventPayload);
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenResourceNotFound() {
    UserResponseDto createdUser = sendCreateUserRequest();
    UUID nonExistentResourceId = UUID.randomUUID();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(), nonExistentResourceId, BigDecimal.valueOf(1.0));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenUserNotFound() {
    UUID nonExistentUserId = UUID.randomUUID();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendDeleteQuantityFromResourceInUserRequest(
            nonExistentUserId, createdResource.getId(), BigDecimal.valueOf(1.8));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenInsufficientQuantity() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourcePurchaseRequestDto(
            createdUser.getId(),
            createdResource.getId(),
            RESOURCE_QUANTITY,
            BigDecimal.valueOf(105.5)));

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(),
            createdResource.getId(),
            RESOURCE_QUANTITY.add(BigDecimal.valueOf(1)));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void removeResourceQuantityFromUserFailsWhenQuantityNegative() {
    UserResponseDto createdUser = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourcePurchaseRequestDto(
            createdUser.getId(),
            createdResource.getId(),
            RESOURCE_QUANTITY,
            BigDecimal.valueOf(105.5)));

    ResponseEntity<ResourcesInUserResponseDto> response =
        sendDeleteQuantityFromResourceInUserRequest(
            createdUser.getId(), createdResource.getId(), BigDecimal.valueOf(-1.0));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void transferResourceFromUserToAnotherUserSuccessfully() throws JsonProcessingException {
    UserResponseDto sender = sendCreateUserRequest();
    PreciousStoneResponseDto createdResource = sendCreatePreciousStoneRequest();
    sendAddResourceInUserRequest(
        createResourcePurchaseRequestDto(
            sender.getId(), createdResource.getId(), RESOURCE_QUANTITY, BigDecimal.valueOf(105.5)));

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
    assertEquals(BigDecimal.valueOf(1), response.getTransferredResource().getQuantity());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response, objectMapper);

    systemEventTestHelper.assertEventWasLogged(RESOURCE_TRANSFER, expectedEventPayload);
  }

  @NotNull
  private static TransferResourceRequestDto getTransferResourceRequestDto(
      UserResponseDto sender, PreciousStoneResponseDto createdResource, UserResponseDto receiver) {
    TransferResourceRequestDto requestDto = new TransferResourceRequestDto();
    requestDto.setPreviousOwnerId(sender.getId());
    requestDto.setNewOwnerId(receiver.getId());
    requestDto.setTransferredResourceId(createdResource.getId());
    requestDto.setQuantity(BigDecimal.valueOf(1));
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

  private ResponseEntity<ResourcesInUserResponseDto> sendDeleteQuantityFromResourceInUserRequest(
      UUID userId, UUID resourceId, BigDecimal quantity) {
    String removeResourceUrl =
        getBaseResourceAvailabilityUrl() + "/" + userId + "/" + resourceId + "/" + quantity;
    return testRestTemplate.exchange(
        removeResourceUrl, HttpMethod.DELETE, HttpEntity.EMPTY, ResourcesInUserResponseDto.class);
  }

  private ResponseEntity<ResourcesInUserResponseDto> sendAddResourceInUserRequest(
      ResourceInUserRequestDto resourceInUserRequestDto) {
    return testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(),
        resourceInUserRequestDto,
        ResourcesInUserResponseDto.class);
  }

  private ResponseEntity<ResourcePurchaseResponseDto> sendPurchaseResourceRequest(
      ResourcePurchaseRequestDto resourcePurchaseRequestDto) {
    return testRestTemplate.postForEntity(
        getBaseResourceAvailabilityUrl(),
        resourcePurchaseRequestDto,
        ResourcePurchaseResponseDto.class);
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
