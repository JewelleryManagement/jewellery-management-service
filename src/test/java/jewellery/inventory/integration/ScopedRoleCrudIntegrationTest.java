package jewellery.inventory.integration;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganizationRequest;
import static jewellery.inventory.helper.ScopedRoleHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createDifferentUserRequest;
import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.common.lang.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.PermissionResponseDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.dto.response.UserWithRolesResponseDto;
import jewellery.inventory.helper.ScopedRoleHelper;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.PermissionScope;
import jewellery.inventory.model.RoleType;
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

  private String getAssignSystemRoleUrl(UUID userId) {
    return "/roles/users/" + userId + "/system-roles";
  }

  private String getBaseOrganizationsUrl() {
    return "/organizations";
  }

  private String getBaseUserUrl() {
    return "/users";
  }

  private ScopedRoleRequestDto scopedOrganizationRoleRequestDto;
  private ScopedRoleRequestDto scopedSystemRoleRequestDto;
  private User userWithoutPermission;

  @BeforeEach
  void setUp() {
    scopedOrganizationRoleRequestDto = createOrganizationRoleRequest();
    scopedSystemRoleRequestDto = createSystemRoleRequest();
    userWithoutPermission = createUserInDatabase(createDifferentUserRequest());
  }

  @Test
  void createRoleSuccessfully() {
    ResponseEntity<ScopedRoleResponseDto> response = createRole(scopedOrganizationRoleRequestDto);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(
        Objects.requireNonNull(response.getBody()).getName(),
        scopedOrganizationRoleRequestDto.getName());
    assertEquals(
        extractPermissions(response.getBody()), scopedOrganizationRoleRequestDto.getPermissions());
  }

  @Test
  void createRoleShouldThrowWhenRoleNameAlreadyExists() {
    createRole(scopedOrganizationRoleRequestDto);

    ResponseEntity<String> response =
        testRestTemplate.postForEntity(
            getBaseRoleUrl(), scopedOrganizationRoleRequestDto, String.class);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains(
                "Role with name: "
                    + scopedOrganizationRoleRequestDto.getName()
                    + " already exists!"));
  }

  @Test
  void createRoleShouldThrowWhenCreatingOrganizationRoleWithSystemPermissions() {
    scopedOrganizationRoleRequestDto.getPermissions().add(Permission.SYSTEM_ROLE_READ);
    scopedOrganizationRoleRequestDto.setName("Test_Role");

    ResponseEntity<String> response =
        testRestTemplate.postForEntity(
            getBaseRoleUrl(), scopedOrganizationRoleRequestDto, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Permission SYSTEM_ROLE_READ is not allowed for role type ORGANIZATION"));
  }

  @Test
  void createRoleShouldThrowWhenCreatingSystemRoleWithOrganizationPermissions() {
    ScopedRoleRequestDto systemRoleRequest = ScopedRoleHelper.createSystemRoleRequest();
    systemRoleRequest.setName("Test_Role");
    systemRoleRequest.getPermissions().add(Permission.ORGANIZATION_READ);

    ResponseEntity<String> response =
        testRestTemplate.postForEntity(getBaseRoleUrl(), systemRoleRequest, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("Permission ORGANIZATION_READ is not allowed for role type SYSTEM"));
  }

  @Test
  void createRoleShouldThrowWhenUserHasNoCreatePermission() {
    authenticateAs(userWithoutPermission);

    scopedOrganizationRoleRequestDto.setName("NEW_ROLE");
    ResponseEntity<String> response =
        testRestTemplate.postForEntity(
            getBaseRoleUrl(), scopedOrganizationRoleRequestDto, String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
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
    ResponseEntity<ScopedRoleResponseDto> role = createRole(scopedOrganizationRoleRequestDto);
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
    ResponseEntity<ScopedRoleResponseDto> role = createRole(scopedOrganizationRoleRequestDto);

    ResponseEntity<Void> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(), HttpMethod.DELETE, null, Void.class);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void deleteRoleShouldThrowWhenUserHasNoDeletePermission() {
    ResponseEntity<ScopedRoleResponseDto> role = createRole(scopedOrganizationRoleRequestDto);

    authenticateAs(userWithoutPermission);

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(), HttpMethod.DELETE, null, String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
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
    ResponseEntity<ScopedRoleResponseDto> role = createRole(scopedOrganizationRoleRequestDto);

    ResponseEntity<ScopedRoleResponseDto> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(),
            HttpMethod.GET,
            null,
            ScopedRoleResponseDto.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(scopedOrganizationRoleRequestDto.getName(), role.getBody().getName());
    assertEquals(
        scopedOrganizationRoleRequestDto.getPermissions(),
        extractPermissions(Objects.requireNonNull(response.getBody())));
  }

  @Test
  void getRoleShouldThrowWhenUserHasNoReadPermission() {
    ResponseEntity<ScopedRoleResponseDto> role = createRole(scopedOrganizationRoleRequestDto);

    authenticateAs(userWithoutPermission);

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/" + role.getBody().getId(), HttpMethod.GET, null, String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getRolesByTypeSuccessfully() {
    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/type/" + RoleType.ORGANIZATION,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getRolesByTypeShouldThrowWhenUserHasNoReadPermission() {
    authenticateAs(userWithoutPermission);

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/type/" + RoleType.ORGANIZATION,
            HttpMethod.GET,
            null,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getAllUserOrganizationRolesWillReturnEmptyArrayWhenUserHasNoUserRolesReadPermission() {
    createOrganizationsWithRequest(getTestOrganizationRequest());
    authenticateAs(userWithoutPermission);

    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/organization/users/" + userWithoutPermission.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllUserOrganizationRolesWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    createOrganizationsWithRequest(getTestOrganizationRequest());

    authenticateAs(userWithoutPermission);

    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/organization/users/" + loggedInAdminUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(0, response.getBody().size());
  }

  @Test
  void getAllUserSystemRolesSuccessfully() {
    ResponseEntity<List<ScopedRoleResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/system/users/" + loggedInAdminUser.getId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ScopedRoleResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getAllUserSystemRolesShouldThrowWhenUserHasNoReadPermission() {
    authenticateAs(userWithoutPermission);

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/system/users/" + loggedInAdminUser.getId(),
            HttpMethod.GET,
            null,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getPermissionsByRoleTypeSuccessfully() {
    ResponseEntity<Set<PermissionResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/permissions?roleType=ORGANIZATION",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Set<PermissionResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(
        getNumberOfPermissionsByScope(PermissionScope.ORGANIZATION), response.getBody().size());
  }

  @Test
  void getPermissionsByRoleTypeShouldThrowWhenUserHasNoPermission() {
    authenticateAs(userWithoutPermission);

    ResponseEntity<String> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/permissions?roleType=ORGANIZATION",
            HttpMethod.GET,
            null,
            String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void getCurrentUserSystemPermissionsSuccessfully() {
    ResponseEntity<Set<PermissionResponseDto>> response =
        testRestTemplate.exchange(
            getBaseRoleUrl() + "/current-user/system-permissions",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Set<PermissionResponseDto>>() {});

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response);
    assertEquals(getNumberOfPermissionsByScope(PermissionScope.SYSTEM), response.getBody().size());
  }

  @Test
  void assignSystemRolesShouldThrowWhenUserNotFound() {
    ScopedRoleResponseDto createdRole = createRole(scopedSystemRoleRequestDto).getBody();

    Set<UUID> roleIds = Set.of(createdRole.getId());
    ResponseEntity<String> response =
        testRestTemplate.postForEntity(
            getAssignSystemRoleUrl(UUID.randomUUID()), roleIds, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void assignSystemRolesShouldReturnEmptyRolesListWhenThereAreNoPassedRoles() {
    Set<UUID> roleIds = new HashSet<>();
    ResponseEntity<UserWithRolesResponseDto> response =
        testRestTemplate.postForEntity(
            getAssignSystemRoleUrl(userWithoutPermission.getId()),
            roleIds,
            UserWithRolesResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response);
    assertEquals(userWithoutPermission.getId(), response.getBody().getUser().getId());
    assertEquals(response.getBody().getRoles().size(), roleIds.size());
  }

  @Test
  void assignSystemRolesSuccessfully() {
    ScopedRoleResponseDto createdRole = createRole(scopedSystemRoleRequestDto).getBody();

    Set<UUID> roleIds = Set.of(createdRole.getId());
    ResponseEntity<UserWithRolesResponseDto> response =
        testRestTemplate.postForEntity(
            getAssignSystemRoleUrl(userWithoutPermission.getId()),
            roleIds,
            UserWithRolesResponseDto.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response);
    assertEquals(userWithoutPermission.getId(), response.getBody().getUser().getId());
    assertEquals(response.getBody().getRoles().size(), roleIds.size());
    assertEquals(response.getBody().getRoles().getFirst().getId(), createdRole.getId());
  }

  @Test
  void assignSystemRolesShouldThrowWhenUserHasNoAssignPermission() {
    ScopedRoleResponseDto createdRole = createRole(scopedSystemRoleRequestDto).getBody();
    Set<UUID> roleIds = Set.of(createdRole.getId());

    authenticateAs(userWithoutPermission);

    ResponseEntity<String> response =
        testRestTemplate.postForEntity(
            getAssignSystemRoleUrl(userWithoutPermission.getId()), roleIds, String.class);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(
        Objects.requireNonNull(response.getBody())
            .contains("You do not have permission to perform this action"));
  }

  @Test
  void assignSystemRolesShouldThrowWhenOrganizationRolesIsPassed(){
    ScopedRoleResponseDto createdRole = createRole(scopedOrganizationRoleRequestDto).getBody();
    Set<UUID> roleIds = Set.of(createdRole.getId());

    ResponseEntity<String> response =
            testRestTemplate.postForEntity(
                    getAssignSystemRoleUrl(userWithoutPermission.getId()), roleIds, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(
            Objects.requireNonNull(response.getBody())
                    .contains("Organization role membership requires an organization"));
  }

  private ResponseEntity<ScopedRoleResponseDto> createRole(
      ScopedRoleRequestDto scopedRoleRequestDtoDto) {
    scopedRoleRequestDtoDto.setName("TEST_ROLE");
    return testRestTemplate.postForEntity(
        getBaseRoleUrl(), scopedRoleRequestDtoDto, ScopedRoleResponseDto.class);
  }

  @Nullable
  private OrganizationResponseDto createOrganizationsWithRequest(OrganizationRequestDto dto) {
    ResponseEntity<OrganizationResponseDto> response =
        this.testRestTemplate.postForEntity(
            getBaseOrganizationsUrl(), dto, OrganizationResponseDto.class);

    return response.getBody();
  }

  @Nullable
  private User createUserInDatabase(UserRequestDto userRequest) {
    ResponseEntity<User> createUser =
        this.testRestTemplate.postForEntity(getBaseUserUrl(), userRequest, User.class);

    return createUser.getBody();
  }

  private int getNumberOfPermissionsByScope(PermissionScope permissionScope) {
    return Arrays.stream(Permission.values())
        .filter(permission -> permission.getPermissionScope() == permissionScope)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)))
        .size();
  }
}
