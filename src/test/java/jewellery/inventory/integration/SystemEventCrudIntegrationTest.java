package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.common.lang.Nullable;
import java.util.*;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SystemEventCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private User userWithoutPermission;

  @BeforeEach
  void setUp() {
    userWithoutPermission = createUserInDatabase(createDifferentUserRequest());
    systemEventRepository.deleteAll();
  }

  @Test
  void testGetAllEventsShouldReturnEmptyList() {
    ResponseEntity<List<SystemEventLiteResponseDto>> eventResponse =
        this.testRestTemplate.exchange(
            getBaseSystemEventUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    List<SystemEventLiteResponseDto> events = eventResponse.getBody();
    assertNotNull(events);
    assertEquals(0, events.size());
  }

  @Test
  void testGetAllEventsSuccessfully() throws JsonProcessingException {
    createAndSaveUser();

    ResponseEntity<List<SystemEventLiteResponseDto>> eventResponse =
        this.testRestTemplate.exchange(
            getBaseSystemEventUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    List<SystemEventLiteResponseDto> events = eventResponse.getBody();
    assertFalse(events != null && events.isEmpty());
    assertNotNull(events, "Retrieved events list is empty");
    assertEquals(EventType.USER_CREATE, events.getFirst().getType());
  }

  @Test
  void testGetAllEventsShouldReturnEmptyListWhenUserHasNoReadPermission() {
    authenticateAs(userWithoutPermission);

    ResponseEntity<List<SystemEventLiteResponseDto>> eventResponse =
        this.testRestTemplate.exchange(
            getBaseSystemEventUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertNotNull(eventResponse);
    assertEquals(0, eventResponse.getBody().size());
  }

  @Test
  void testGetAllEventsShouldReturnOnlyOrganizationEventsWhenUserHasOrganizationReadPermission() {
    createAndSaveUser();
    OrganizationResponseDto organizationsWithRequest =
        createOrganizationsWithRequest(getTestOrganizationRequest());
    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_EVENT_READ);
    ScopedRoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(
        userWithoutPermission.getId(), organizationsWithRequest.getId(), newRole.getId());
    authenticateAs(userWithoutPermission);

    ResponseEntity<List<SystemEventLiteResponseDto>> eventResponse =
        this.testRestTemplate.exchange(
            getBaseSystemEventUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertNotNull(eventResponse);
    assertEquals(1, eventResponse.getBody().size());
    assertEquals(EventType.ORGANIZATION_CREATE, eventResponse.getBody().getFirst().getType());
  }

  @Test
  void testGetSystemEventShouldThrow() {
    UUID randomId = UUID.randomUUID();

    ResponseEntity<String> response =
        testRestTemplate.exchange(getSystemEventUrl(randomId), HttpMethod.GET, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Event with id: " + randomId + " is not found!"));
  }

  @Test
  void testGetSystemEventSuccessfully() {
    DetailedUserResponseDto user = createAndSaveUser();

    ResponseEntity<List<SystemEventLiteResponseDto>> eventWithRelatedId =
        getRelatedEventsResponse(user.getId());

    ResponseEntity<SystemEventResponseDto> response =
        testRestTemplate.getForEntity(
            getSystemEventUrl(
                Objects.requireNonNull(eventWithRelatedId.getBody()).getFirst().getId()),
            SystemEventResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertNotNull(Objects.requireNonNull(response.getBody()).getPayload());

    Map<String, Object> expectedEventPayload = getCreateOrDeleteEventPayload(user, objectMapper);

    assertEquals(response.getBody().getPayload(), expectedEventPayload);
    assertEquals(EventType.USER_CREATE, response.getBody().getType());
  }

  @Test
  void testGetEventsRelatedToShouldReturnEmptyList() {
    UUID randomId = UUID.randomUUID();

    ResponseEntity<List<SystemEventLiteResponseDto>> eventWithRelatedId =
        getRelatedEventsResponse(randomId);

    assertEquals(HttpStatus.OK, eventWithRelatedId.getStatusCode());
    assertNotNull(eventWithRelatedId);
    assertEquals(0, Objects.requireNonNull(eventWithRelatedId.getBody()).size());
  }

  @Test
  void testGetEventsRelatedToSuccessfully() {
    DetailedUserResponseDto user = createAndSaveUser();

    ResponseEntity<List<SystemEventLiteResponseDto>> eventWithRelatedId =
        getRelatedEventsResponse(user.getId());

    assertEquals(HttpStatus.OK, eventWithRelatedId.getStatusCode());
    assertNotNull(eventWithRelatedId);
    assertEquals(1, Objects.requireNonNull(eventWithRelatedId.getBody()).size());
    assertEquals(EventType.USER_CREATE, eventWithRelatedId.getBody().getFirst().getType());
  }

  @Test
  void testGetEventsRelatedToShouldReturnEmptyListWhenUserHasNoReadPermission() {
    createAndSaveUser();

    authenticateAs(userWithoutPermission);

    ResponseEntity<List<SystemEventLiteResponseDto>> eventWithRelatedId =
        getRelatedEventsResponse(loggedInAdminUser.getId());

    assertNotNull(eventWithRelatedId);
    assertEquals(0, eventWithRelatedId.getBody().size());
  }

  @Test
  void
      testGetEventsRelatedToShouldReturnOnlyOrganizationEventsWhenUserHasOrganizationReadPermission() {
    createAndSaveUser();
    OrganizationResponseDto organizationsWithRequest =
        createOrganizationsWithRequest(getTestOrganizationRequest());
    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_EVENT_READ);
    ScopedRoleResponseDto newRole = createRole("Test", permissions);
    createRoleMembership(
        userWithoutPermission.getId(), organizationsWithRequest.getId(), newRole.getId());
    authenticateAs(userWithoutPermission);

    ResponseEntity<List<SystemEventLiteResponseDto>> eventWithRelatedId =
        getRelatedEventsResponse(loggedInAdminUser.getId());

    assertNotNull(eventWithRelatedId);
    assertEquals(1, eventWithRelatedId.getBody().size());
    assertEquals(EventType.ORGANIZATION_CREATE, eventWithRelatedId.getBody().getFirst().getType());
  }

  private String getBaseSystemEventUrl() {
    return "/system-events";
  }

  private String getSystemEventUrl(UUID id) {
    return getBaseSystemEventUrl() + "/" + id;
  }

  private String getSystemEventRelatedToUrl(UUID id) {
    return getBaseSystemEventUrl() + "/related-to/" + id;
  }

  private String getBaseUserUrl() {
    return "/users";
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private DetailedUserResponseDto createAndSaveUser() {
    UserRequestDto userRequest = createTestUserRequest();

    return this.testRestTemplate
        .postForEntity("/users", userRequest, DetailedUserResponseDto.class)
        .getBody();
  }

  private ResponseEntity<List<SystemEventLiteResponseDto>> getRelatedEventsResponse(UUID id) {
    return testRestTemplate.exchange(
        getSystemEventRelatedToUrl(id),
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {});
  }

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequest) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, User.class);

    return createUser.getBody();
  }

  @Nullable
  private OrganizationResponseDto createOrganizationsWithRequest(OrganizationRequestDto dto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), dto, OrganizationResponseDto.class);

    return response.getBody();
  }
}
