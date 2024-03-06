package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.SystemEventTestHelper.getCreateOrDeleteEventPayload;
import static jewellery.inventory.model.EventType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.common.lang.Nullable;
import java.util.*;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserInOrganizationRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.*;
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

  private String getOrganizationByIdUrl(UUID organizationId) {
    return "/organizations/" + organizationId;
  }

  private String getOrganizationUsersUrl(UUID organizationId, UUID userId) {
    return "/organizations/" + organizationId + "/users/" + userId;
  }

  private String getOrganizationUsersUrl(UUID organizationId) {
    return "/organizations/" + organizationId + "/users";
  }

  private Organization organization;
  private OrganizationRequestDto organizationRequestDto;
  private User user;
  private UserInOrganizationRequestDto userInOrganizationRequestDto;

  @BeforeEach
  void setUp() {
    organization = getTestOrganization();
    organizationRequestDto = getTestOrganizationRequest();
    user = createUserInDatabase(UserTestHelper.createDifferentUserRequest());
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

    ResponseEntity<OrganizationMembersResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationId),
            HttpMethod.GET,
            null,
            OrganizationMembersResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void deleteUserInOrganizationSuccessfully() {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();
    addUserInOrganization(organizationId);

    ResponseEntity<HttpStatus> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationId, user.getId()),
            HttpMethod.DELETE,
            null,
            HttpStatus.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
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
    addUserInOrganization(organizationId);

    UpdateUserInOrganizationRequest request = new UpdateUserInOrganizationRequest();
    request.setOrganizationPermission(Arrays.asList(OrganizationPermission.values()));

    ResponseEntity<OrganizationSingleMemberResponseDto> response =
        this.testRestTemplate.exchange(
            getOrganizationUsersUrl(organizationId, user.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            OrganizationSingleMemberResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void addUserInOrganizationSuccessfully() throws JsonProcessingException {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    ResponseEntity<OrganizationSingleMemberResponseDto> response =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationId),
            userInOrganizationRequestDto,
            OrganizationSingleMemberResponseDto.class);

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
