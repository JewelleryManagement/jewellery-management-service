package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static jewellery.inventory.helper.UserTestHelper.createTestUserRequest;
import static jewellery.inventory.model.EventType.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.common.lang.Nullable;
import java.util.*;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserInOrganizationRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.model.OrganizationRole;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

class OrganizationCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private String getOrganizationByIdUrl(UUID organizationId) {
    return "/organizations/" + organizationId;
  }

  private String getOrganizationUsersUrl(UUID organizationId, UUID userId) {
    return "/organizations/" + organizationId + "/users/" + userId;
  }

  private String getOrganizationUsersUrl(UUID organizationId) {
    return "/organizations/" + organizationId + "/users";
  }

  private OrganizationRequestDto organizationRequestDto;
  private User user;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;
  private OrganizationResponseDto organizationResponseDto;

  @BeforeEach
  void setUp() {
    organizationRequestDto = getTestOrganizationRequest();
    organizationResponseDto = createOrganizationsWithRequest(organizationRequestDto);
    OrganizationRole roleWithAllPermissions = createRoleWithAllPermissions();
    createOrganizationMembership(
        loggedInAdminUser.getId(), organizationResponseDto.getId(), roleWithAllPermissions.getId());
    user = createUserInDatabase(createTestUserRequest());
    userInOrganizationRequestDto = getTestUserInOrganizationRequest(user.getId());
  }

  @Test
  void deleteOrganizationSuccessfully() throws JsonProcessingException {
    ResponseEntity<HttpStatus> response =
        this.testRestTemplate.exchange(
            getOrganizationByIdUrl(organizationResponseDto.getId()),
            HttpMethod.DELETE,
            null,
            HttpStatus.class);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(organizationResponseDto, objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_DELETE, expectedEventPayload, organizationResponseDto.getId());
  }

  @Test
  void getOrganizationsSuccessfully() {
    ResponseEntity<List<OrganizationResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseOrganizationsUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertNotNull(response.getBody());
  }

  //  @Test
  //  void getOrganizationByIdNotFound() {
  //    ResponseEntity<OrganizationResponseDto> response =
  //        this.testRestTemplate.getForEntity(
  //            getOrganizationByIdUrl(UUID.randomUUID()),
  //            OrganizationResponseDto.class);
  //
  //    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(404));
  //  }

  @Test
  void getOrganizationByIdSuccessfully() {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.getForEntity(
            getOrganizationByIdUrl(organizationResponseDto.getId()), OrganizationResponseDto.class);

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
  }

  @Test
  void getAllUsersInOrganizationSuccessfully() {
    ResponseEntity<OrganizationMembersResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationResponseDto.getId()),
            HttpMethod.GET,
            null,
            OrganizationMembersResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void getUserInOrganizationSuccessfully() {
    OrganizationSingleMemberResponseDto userInOrganization =
        addUserInOrganization(organizationResponseDto.getId());

    ResponseEntity<UserInOrganizationResponseDto> response =
        this.testRestTemplate.getForEntity(
            getOrganizationUsersUrl(
                organizationResponseDto.getId(), userInOrganization.getMember().getUser().getId()),
            UserInOrganizationResponseDto.class);
    assertNotNull(response);
    assertEquals(
        userInOrganization.getMember().getUser().getId(),
        Objects.requireNonNull(response.getBody()).getUser().getId());
  }

  @Test
  void getUserInOrganizationShouldThrowUserNotFoundException() {
    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getOrganizationUsersUrl(organizationResponseDto.getId(), user.getId()), String.class);
    assertNotNull(response);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("User with id " + user.getId() + " was not found"));
  }

  @Test
  void deleteUserInOrganizationSuccessfully() throws JsonProcessingException {
    OrganizationSingleMemberResponseDto singleMemberResponseDto =
        addUserInOrganization(organizationResponseDto.getId());

    ResponseEntity<HttpStatus> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationResponseDto.getId(), user.getId()),
            HttpMethod.DELETE,
            null,
            HttpStatus.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(singleMemberResponseDto, objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_USER_DELETE, expectedEventPayload, user.getId());
  }

  @Test
  void updateUserInOrganizationSuccessfully() throws JsonProcessingException {
    OrganizationSingleMemberResponseDto singleMemberResponseDto =
        addUserInOrganization(organizationResponseDto.getId());

    ResponseEntity<OrganizationSingleMemberResponseDto> response =
        removePermissionsForUser(organizationResponseDto, user);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    Map<String, Object> expectedEventPayload =
        getUpdateEventPayload(singleMemberResponseDto, response.getBody(), objectMapper);
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_USER_UPDATE, expectedEventPayload, user.getId());
  }

  @Test
  void updateUserInOrganizationWithNullPermissions() {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationResponseDto.getId(), user.getId()),
            HttpMethod.PUT,
            null,
            OrganizationResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void addUserInOrganizationSuccessfully() throws JsonProcessingException {
    ResponseEntity<OrganizationSingleMemberResponseDto> response =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationResponseDto.getId()),
            userInOrganizationRequestDto,
            OrganizationSingleMemberResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_USER_CREATE, expectedEventPayload, userInOrganizationRequestDto.getUserId());
  }

  @Test
  void addUserInOrganizationThrowUserIsPartOfOrganizationException() {
    ResponseEntity<OrganizationSingleMemberResponseDto> response =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationResponseDto.getId()),
            userInOrganizationRequestDto,
            OrganizationSingleMemberResponseDto.class);

    ResponseEntity<OrganizationSingleMemberResponseDto> responseSameUser =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationResponseDto.getId()),
            userInOrganizationRequestDto,
            OrganizationSingleMemberResponseDto.class);

    assertEquals(HttpStatus.CONFLICT, responseSameUser.getStatusCode());
  }

  @Test
  void createOrganizationSuccessfully() throws JsonProcessingException {
    ResponseEntity<OrganizationResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), organizationRequestDto, OrganizationResponseDto.class);

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(201));
    assertNotNull(response.getBody());
    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);
    systemEventTestHelper.assertEventWasLogged(
        ORGANIZATION_CREATE, expectedEventPayload, response.getBody().getId());
  }

  @Test
  void getAllOrganizationsForCurrentUserShouldReturnEmptyListIfOrganizationReadPermissionMissing() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<List<OrganizationResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseOrganizationsUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllOrganizationsForCurrentUserShouldReturnOnlyOrganizationThatUserHasPermissionToRead() {
    OrganizationResponseDto secondOrganization =
        createOrganizationsWithRequest(getTestOrganizationRequest());
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    Set<Permission> permissions = Set.of(Permission.ORGANIZATION_READ);
    OrganizationRole newRole = createRole("Test", permissions);
    createOrganizationMembership(deniedUser.getId(), secondOrganization.getId(), newRole.getId());

    ResponseEntity<List<OrganizationResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseOrganizationsUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getOrganizationByIdShouldThrowWhenUserHasNoReadPermission() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getOrganizationByIdUrl(organizationResponseDto.getId()), String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void addUserInOrganizationShouldThrowWhenUserHasNoUserAddPermission() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationResponseDto.getId()),
            userInOrganizationRequestDto,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void deleteUserInOrganizationShouldThrowWhenUserHasNoUserDeletePermission() {
    addUserInOrganization(organizationResponseDto.getId());
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationResponseDto.getId(), user.getId()),
            HttpMethod.DELETE,
            null,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void deleteOrganizationShouldThrowWhenUserHasNoDeletePermission() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            getOrganizationByIdUrl(organizationResponseDto.getId()),
            HttpMethod.DELETE,
            null,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getAllUsersInOrganizationShouldThrowWhenUserHasNoUserReadPermission() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationResponseDto.getId()),
            HttpMethod.GET,
            null,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getUserInOrganizationShouldThrowWhenUserHasNoUserReadPermission() {
    OrganizationSingleMemberResponseDto userInOrganization =
        addUserInOrganization(organizationResponseDto.getId());
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);

    ResponseEntity<String> response =
        this.testRestTemplate.getForEntity(
            getOrganizationUsersUrl(
                organizationResponseDto.getId(), userInOrganization.getMember().getUser().getId()),
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  //  @Test
  //  void getOrganizationsByPermissionSuccessfully() {
  //    ResponseEntity<OrganizationResponseDto> firstOrganization =
  //        testRestTemplate.postForEntity(
  //            getBaseOrganizationsUrl(), organizationRequestDto, OrganizationResponseDto.class);
  //    organizationRequestDto.setName("secondOrg");
  //    ResponseEntity<OrganizationResponseDto> secondOrganization =
  //        testRestTemplate.postForEntity(
  //            getBaseOrganizationsUrl(), organizationRequestDto, OrganizationResponseDto.class);
  //    removePermissionsForUser(secondOrganization.getBody(), loggedInAdminUser);
  //
  //    ResponseEntity<List<OrganizationResponseDto>> organizations =
  //        this.testRestTemplate.exchange(
  //            getBaseOrganizationsUrl()
  //                + "/by-permission/"
  //                + OrganizationPermission.CREATE_PRODUCT.name(),
  //            HttpMethod.GET,
  //            null,
  //            new ParameterizedTypeReference<>() {});
  //
  //    assertEquals(1, organizations.getBody().size());
  //    assertEquals(organizations.getBody().get(0).getId(), firstOrganization.getBody().getId());
  //  }

  private ResponseEntity<OrganizationSingleMemberResponseDto> removePermissionsForUser(
      OrganizationResponseDto secondOrganization, User user) {
    UpdateUserInOrganizationRequest request = new UpdateUserInOrganizationRequest();
    request.setOrganizationPermission(new ArrayList<>());
    ResponseEntity<OrganizationSingleMemberResponseDto> changedPermissionsOrganization =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(secondOrganization.getId(), user.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            OrganizationSingleMemberResponseDto.class);
    return changedPermissionsOrganization;
  }

  @Nullable
  private OrganizationResponseDto createOrganizationsWithRequest(OrganizationRequestDto dto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), dto, OrganizationResponseDto.class);

    return response.getBody();
  }

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequestDto) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity("/users", userRequestDto, User.class);
    return createUser.getBody();
  }

  @Nullable
  private OrganizationSingleMemberResponseDto addUserInOrganization(UUID organizationId) {
    ResponseEntity<OrganizationSingleMemberResponseDto> response =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationId),
            userInOrganizationRequestDto,
            OrganizationSingleMemberResponseDto.class);
    return response.getBody();
  }
}
