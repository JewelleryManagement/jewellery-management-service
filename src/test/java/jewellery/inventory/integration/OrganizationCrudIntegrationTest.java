package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micrometer.common.lang.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

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
    ResponseEntity<List<ProductResponseDto>> response =
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
  void AddUserInOrganizationSuccessfully() {
    UUID organizationId = createOrganizationsWithRequest(organizationRequestDto).getId();

    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getOrganizationUsersUrl(organizationId),
            userInOrganizationRequestDto,
            OrganizationResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void createOrganizationSuccessfully() {
    ResponseEntity<OrganizationResponseDto> response =
        testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), organizationRequestDto, OrganizationResponseDto.class);

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(201));
    assertNotNull(response.getBody());
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
