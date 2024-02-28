package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.helper.SystemEventTestHelper.getUpdateEventPayload;
import static jewellery.inventory.model.EventType.*;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.common.lang.Nullable;
import java.util.*;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserPermissionsRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.OrganizationPermission;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

class OrganizationCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private String getOrganizationByIdUrl(UUID id) {
    return "/organizations/" + id;
  }

  private String getOrganizationUsersUrl(UUID organization, UUID user) {
    return "/organizations/" + organization + "/users/" + user;
  }

  private String getOrganizationUsersUrl(UUID organization) {
    return "/organizations/" + organization + "/users";
  }

  private Organization organization;
  private OrganizationRequestDto organizationRequestDto;
  private User user;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;

  @BeforeEach
  void setUp() {
    organization = getTestOrganization();
    organizationRequestDto = getTestOrganizationRequest();
    user = createUserInDatabase(UserTestHelper.createTestUserRequest());
    userInOrganizationRequestDto = getTestUserInOrganizationRequest(user.getId());
  }

  @Test
  void getOrganizationsSuccessfully() {
    ResponseEntity<List<OrganizationResponseDto>> response =
        this.testRestTemplate.exchange(
            getBaseOrganizationsUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertNotNull(response.getBody());
  }

  @Test
  void getOrganizationByIdNotFound() {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.getForEntity(
            getOrganizationByIdUrl(Objects.requireNonNull(organization).getId()),
            OrganizationResponseDto.class);

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(404));
  }

  @Test
  void getOrganizationByIdSuccessfully() {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.getForEntity(
            getOrganizationByIdUrl(organizationId), OrganizationResponseDto.class);

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
  }

  @Test
  void getAllUsersInOrganizationSuccessfully() {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    ParameterizedTypeReference<List<UserInOrganizationResponseDto>> responseType =
        new ParameterizedTypeReference<List<UserInOrganizationResponseDto>>() {};

    ResponseEntity<List<UserInOrganizationResponseDto>> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationId), HttpMethod.GET, null, responseType);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void deleteUserInOrganizationSuccessfully() {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationId, user.getId()),
            HttpMethod.DELETE,
            null,
            OrganizationResponseDto.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void updateUserInOrganizationWithNullPermissions() {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationId, user.getId()),
            HttpMethod.PUT,
            null,
            OrganizationResponseDto.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void updateUserInOrganizationSuccessfully() {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    UpdateUserPermissionsRequest request = new UpdateUserPermissionsRequest();
    request.setOrganizationPermission(Arrays.asList(OrganizationPermission.values()));

    ResponseEntity<UserInOrganizationResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationId, user.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
                UserInOrganizationResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void addUserInOrganizationSuccessfully() throws JsonProcessingException {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    ResponseEntity<UserInOrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationId),
            userInOrganizationRequestDto,
            UserInOrganizationResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());

    Map<String, Object> expectedEventPayload =
        getCreateOrDeleteEventPayload(response.getBody(), objectMapper);

    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_USER_CREATE, expectedEventPayload);
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

    systemEventTestHelper.assertEventWasLogged(ORGANIZATION_CREATE, expectedEventPayload);
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
}
