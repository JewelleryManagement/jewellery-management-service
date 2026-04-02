package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.RoleHelper.createRoleRequest;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.common.lang.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.RoleRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.RoleResponseDto;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RoleCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private String getBaseRoleUrl() {
    return "/roles";
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private RoleRequestDto roleRequestDto;

  @BeforeEach
  void setUp() {
    roleRequestDto = createRoleRequest();
  }

  @Test
  void createRoleSuccessfully() {
    ResponseEntity<RoleResponseDto> response = createRole();

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(Objects.requireNonNull(response.getBody()).getName(), roleRequestDto.getName());
    assertEquals(response.getBody().getPermissions(), roleRequestDto.getPermissions());
  }

  @Test
  void createRoleShouldThrowWhenRoleNameAlreadyExists() {
    createRole();

    ResponseEntity<String> response =
        testRestTemplate.postForEntity(getBaseRoleUrl(), roleRequestDto, String.class);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Role with name: " + roleRequestDto.getName() + " already exists!"));
  }

  @Test
  void deleteRoleShouldThrowWhenRoleDoesNotExists() {
    UUID roleId = UUID.randomUUID();

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + roleId, HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Role with id: " + roleId + " does not exists!"));
  }

  @Test
  void deleteRoleShouldThrowWhenRoleAlreadyAssigned() {
    OrganizationResponseDto organizationResponseDto =
        createOrganizationsWithRequest(getTestOrganizationRequest());
    ResponseEntity<RoleResponseDto> role = createRole();
    createRoleMembership(
        loggedInAdminUser.getId(), organizationResponseDto.getId(), role.getBody().getId());

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(), HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Role is assigned to members and cannot be deleted"));
  }

  @Test
  void deleteRoleSuccessfully() {
    ResponseEntity<RoleResponseDto> role = createRole();

    ResponseEntity<Void> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(), HttpMethod.DELETE, null, Void.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void getRoleShouldThrowWhenRoleDoesNotExists() {
    UUID roleId = UUID.randomUUID();

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + roleId, HttpMethod.GET, null, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Role with id: " + roleId + " does not exists!"));
  }

  @Test
  void getRoleSuccessfully() {
    ResponseEntity<RoleResponseDto> role = createRole();

    ResponseEntity<RoleResponseDto> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(),
            HttpMethod.GET,
            null,
            RoleResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(roleRequestDto.getName(), role.getBody().getName());
    assertEquals(roleRequestDto.getPermissions(), role.getBody().getPermissions());
  }

  @Test
  void getAllRolesSuccessfully() {
    ResponseEntity<List<RoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<RoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getAllUserRolesWillReturnEmptyArrayWhenUserHasNoUserRolesReadPermission() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);
    createOrganizationsWithRequest(getTestOrganizationRequest());
    authenticateAs(loggedInAdminUser);

    ResponseEntity<List<RoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/users/" + deniedUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<RoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllUserRolesWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    createOrganizationsWithRequest(getTestOrganizationRequest());

    ResponseEntity<List<RoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/users/" + loggedInAdminUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<RoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getAllUserRolesByOrganizationWillReturnEmptyArrayWhenUserHasNoUserRolesReadPermission() {
    User deniedUser = createAndPersistUser(createDifferentUserRequest());
    authenticateAs(deniedUser);
    OrganizationResponseDto organizationResponseDto =
        createOrganizationsWithRequest(getTestOrganizationRequest());
    authenticateAs(loggedInAdminUser);

    ResponseEntity<List<RoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl()
                + "/organizations/"
                + organizationResponseDto.getId()
                + "/users/"
                + deniedUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<RoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllUserRolesByOrganizationWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    OrganizationResponseDto organizationResponseDto =
        createOrganizationsWithRequest(getTestOrganizationRequest());

    ResponseEntity<List<RoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl()
                + "/organizations/"
                + organizationResponseDto.getId()
                + "/users/"
                + loggedInAdminUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<RoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
  }

  private ResponseEntity<RoleResponseDto> createRole() {
    roleRequestDto.setName("TEST_ROLE");
    return testRestTemplate.postForEntity(getBaseRoleUrl(), roleRequestDto, RoleResponseDto.class);
  }

  @Nullable
  private OrganizationResponseDto createOrganizationsWithRequest(OrganizationRequestDto dto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), dto, OrganizationResponseDto.class);

    return response.getBody();
  }
}
