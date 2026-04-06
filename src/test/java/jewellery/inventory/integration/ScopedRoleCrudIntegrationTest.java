package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.ScopedRoleHelper.createRoleRequest;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.common.lang.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ScopedRoleCrudIntegrationTest extends AuthenticatedIntegrationTestBase {
  private String getBaseRoleUrl() {
    return "/roles";
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private ScopedRoleRequestDto scopedRoleRequestDto;

  @BeforeEach
  void setUp() {
    scopedRoleRequestDto = createRoleRequest();
  }

  @Test
  void createRoleSuccessfully() {
    ResponseEntity<ScopedRoleResponseDto> response = createRole();

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(Objects.requireNonNull(response.getBody()).getName(), scopedRoleRequestDto.getName());
    assertEquals(response.getBody().getPermissions(), scopedRoleRequestDto.getPermissions());
  }

  @Test
  void createRoleShouldThrowWhenRoleNameAlreadyExists() {
    createRole();

    ResponseEntity<String> response =
        testRestTemplate.postForEntity(getBaseRoleUrl(), scopedRoleRequestDto, String.class);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Role with name: " + scopedRoleRequestDto.getName() + " already exists!"));
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
    ResponseEntity<ScopedRoleResponseDto> role = createRole();
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
    ResponseEntity<ScopedRoleResponseDto> role = createRole();

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
    ResponseEntity<ScopedRoleResponseDto> role = createRole();

    ResponseEntity<ScopedRoleResponseDto> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(),
            HttpMethod.GET,
            null,
            ScopedRoleResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(scopedRoleRequestDto.getName(), role.getBody().getName());
    assertEquals(scopedRoleRequestDto.getPermissions(), role.getBody().getPermissions());
  }

  @Test
  void getAllRolesSuccessfully() {
    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

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

    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/users/" + deniedUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllUserRolesWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    createOrganizationsWithRequest(getTestOrganizationRequest());

    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/users/" + loggedInAdminUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

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

    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl()
                + "/organizations/"
                + organizationResponseDto.getId()
                + "/users/"
                + deniedUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllUserRolesByOrganizationWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    OrganizationResponseDto organizationResponseDto =
        createOrganizationsWithRequest(getTestOrganizationRequest());

    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl()
                + "/organizations/"
                + organizationResponseDto.getId()
                + "/users/"
                + loggedInAdminUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
  }

  private ResponseEntity<ScopedRoleResponseDto> createRole() {
    scopedRoleRequestDto.setName("TEST_ROLE");
    return testRestTemplate.postForEntity(getBaseRoleUrl(), scopedRoleRequestDto, ScopedRoleResponseDto.class);
  }

  @Nullable
  private OrganizationResponseDto createOrganizationsWithRequest(OrganizationRequestDto dto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), dto, OrganizationResponseDto.class);

    return response.getBody();
  }
}
