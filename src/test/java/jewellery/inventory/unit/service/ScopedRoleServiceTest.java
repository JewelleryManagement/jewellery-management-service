package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ScopedRoleHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.PermissionResponseDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.NoAuthenticatedUserException;
import jewellery.inventory.exception.not_found.RoleNotFoundException;
import jewellery.inventory.exception.role.RoleAlreadyAssignedException;
import jewellery.inventory.exception.role.RoleNameAlreadyExistsException;
import jewellery.inventory.mapper.ScopedRoleMapper;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleType;
import jewellery.inventory.model.ScopedRole;
import jewellery.inventory.repository.RoleMembershipRepository;
import jewellery.inventory.repository.ScopedRoleRepository;
import jewellery.inventory.service.ScopedRoleService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScopedRoleServiceTest {
  @InjectMocks private ScopedRoleService scopedRoleService;
  @Mock private ScopedRoleRepository scopedRoleRepository;
  @Mock private ScopedRoleMapper scopedRoleMapper;
  @Mock private RoleMembershipRepository roleMembershipRepository;
  @Mock private AuthService authService;

  private ScopedRoleRequestDto scopedOrganizationRoleRequestDto;
  private ScopedRoleRequestDto scopedSystemRoleRequestDto;
  private ScopedRole scopedOrganizationRole;
  private ScopedRole scopedSystemRole;
  private ScopedRoleResponseDto scopedOrganizationRoleResponseDto;
  private ScopedRoleResponseDto scopedSystemRoleResponseDto;
  private UserResponseDto currentUser;
  private UserResponseDto targetUser;

  @BeforeEach
  void setUp() {
    scopedOrganizationRoleRequestDto = createOrganizationRoleRequest();
    scopedSystemRoleRequestDto = createSystemRoleRequest();
    scopedOrganizationRole = createRole(scopedOrganizationRoleRequestDto);
    scopedSystemRole = createRole(scopedSystemRoleRequestDto);
    scopedOrganizationRoleResponseDto = createRoleResponse(scopedOrganizationRole);
    scopedSystemRoleResponseDto = createRoleResponse(scopedSystemRole);
    currentUser = createTestUserResponseDto(createTestAdminUser());
    targetUser = createTestUserResponseDto(createTestUser());
  }

  @Test
  void createRoleShouldThrowWhenRoleNameAlreadyExists() {
    when(scopedRoleRepository.existsByName(scopedOrganizationRoleRequestDto.getName()))
        .thenThrow(RoleNameAlreadyExistsException.class);

    assertThrows(
        RoleNameAlreadyExistsException.class,
        () -> scopedRoleService.createRole(scopedOrganizationRoleRequestDto));
  }

  @Test
  void createRoleSuccessfully() {
    when(scopedRoleRepository.existsByName(scopedOrganizationRoleRequestDto.getName()))
        .thenReturn(false);
    when(scopedRoleRepository.save(any(ScopedRole.class))).thenReturn(scopedOrganizationRole);
    when(scopedRoleMapper.toResponse(scopedOrganizationRole))
        .thenReturn(this.scopedOrganizationRoleResponseDto);

    ScopedRoleResponseDto scopedRoleResponseDto =
        scopedRoleService.createRole(scopedOrganizationRoleRequestDto);

    assertNotNull(scopedRoleResponseDto);
    assertEquals(scopedRoleResponseDto.getName(), scopedOrganizationRoleRequestDto.getName());
    assertEquals(
        extractPermissions(scopedRoleResponseDto),
        scopedOrganizationRoleRequestDto.getPermissions());
    verify(scopedRoleRepository, times(1)).existsByName(scopedOrganizationRoleRequestDto.getName());
    verify(scopedRoleRepository, times(1)).save(any(ScopedRole.class));
    verify(scopedRoleMapper, times(1)).toResponse(scopedOrganizationRole);
  }

  @Test
  void getRoleByNameShouldThrowWhenRoleWithGivenNameDoesNotExists() {
    when(scopedRoleRepository.findByName(scopedOrganizationRoleRequestDto.getName()))
        .thenThrow(RoleNotFoundException.class);

    assertThrows(
        RoleNotFoundException.class,
        () -> scopedRoleService.getRoleByName(scopedOrganizationRoleRequestDto.getName()));
  }

  @Test
  void getRoleByNameSuccessfully() {
    when(scopedRoleRepository.findByName(scopedOrganizationRoleRequestDto.getName()))
        .thenReturn(Optional.ofNullable(scopedOrganizationRole));

    ScopedRole role = scopedRoleService.getRoleByName(scopedOrganizationRoleRequestDto.getName());

    assertNotNull(role);
    assertEquals(role.getName(), scopedOrganizationRoleRequestDto.getName());
    assertEquals(role.getPermissions(), scopedOrganizationRoleRequestDto.getPermissions());
    verify(scopedRoleRepository, times(1)).findByName(scopedOrganizationRoleRequestDto.getName());
  }

  @Test
  void deleteRoleShouldThrowWhenRoleDoesNotExists() {
    when(scopedRoleRepository.findById(scopedOrganizationRole.getId()))
        .thenThrow(RoleNotFoundException.class);

    assertThrows(
        RoleNotFoundException.class,
        () -> scopedRoleService.deleteRole(scopedOrganizationRole.getId()));
  }

  @Test
  void deleteRoleShouldThrowWhenRoleAlreadyAssigned() {
    when(scopedRoleRepository.findById(scopedOrganizationRole.getId()))
        .thenReturn(Optional.ofNullable(scopedOrganizationRole));
    when(roleMembershipRepository.existsByRoleId(scopedOrganizationRole.getId()))
        .thenThrow(RoleAlreadyAssignedException.class);

    assertThrows(
        RoleAlreadyAssignedException.class,
        () -> scopedRoleService.deleteRole(scopedOrganizationRole.getId()));
  }

  @Test
  void deleteRoleSuccessfully() {
    when(scopedRoleRepository.findById(scopedOrganizationRole.getId()))
        .thenReturn(Optional.ofNullable(scopedOrganizationRole));
    when(roleMembershipRepository.existsByRoleId(scopedOrganizationRole.getId())).thenReturn(false);

    scopedRoleService.deleteRole(scopedOrganizationRole.getId());

    verify(scopedRoleRepository, times(1)).findById(scopedOrganizationRole.getId());
    verify(roleMembershipRepository, times(1)).existsByRoleId(scopedOrganizationRole.getId());
  }

  @Test
  void getRoleShouldThrowWhenRoleDoesNotExists() {
    when(scopedRoleRepository.findById(scopedOrganizationRole.getId()))
        .thenThrow(RoleNotFoundException.class);

    assertThrows(
        RoleNotFoundException.class,
        () -> scopedRoleService.getRole(scopedOrganizationRole.getId()));
  }

  @Test
  void getRoleSuccessfully() {
    when(scopedRoleRepository.findById(scopedOrganizationRole.getId()))
        .thenReturn(Optional.ofNullable(scopedOrganizationRole));
    when(scopedRoleMapper.toResponse(scopedOrganizationRole))
        .thenReturn(scopedOrganizationRoleResponseDto);

    ScopedRoleResponseDto role = scopedRoleService.getRole(scopedOrganizationRole.getId());

    assertNotNull(role);
    assertEquals(role.getName(), scopedOrganizationRole.getName());
    assertEquals(extractPermissions(role), scopedOrganizationRole.getPermissions());
    verify(scopedRoleRepository, times(1)).findById(scopedOrganizationRole.getId());
  }

  @Test
  void getRolesByTypeShouldReturnEmptyArrayWhenThereAreNoRolesOfGivenType() {
    when(scopedRoleRepository.findByRoleType(RoleType.ORGANIZATION))
        .thenReturn(Collections.emptyList());

    List<ScopedRoleResponseDto> roles = scopedRoleService.getRolesByType(RoleType.ORGANIZATION);

    assertNotNull(roles);
    assertTrue(roles.isEmpty());
    verify(scopedRoleRepository).findByRoleType(RoleType.ORGANIZATION);
  }

  @Test
  void getRolesByTypeSuccessfully() {
    when(scopedRoleRepository.findByRoleType(RoleType.ORGANIZATION))
        .thenReturn(List.of(scopedOrganizationRole));
    when(scopedRoleMapper.toResponse(scopedOrganizationRole))
        .thenReturn(scopedOrganizationRoleResponseDto);

    List<ScopedRoleResponseDto> roles = scopedRoleService.getRolesByType(RoleType.ORGANIZATION);

    assertNotNull(roles);
    assertEquals(1, roles.size());
    assertEquals(scopedOrganizationRole.getId(), roles.getFirst().getId());
    assertEquals(scopedOrganizationRole.getName(), roles.getFirst().getName());
    assertEquals(scopedOrganizationRole.getPermissions(), extractPermissions(roles.getFirst()));
    verify(scopedRoleRepository, times(1)).findByRoleType(RoleType.ORGANIZATION);
    verify(scopedRoleMapper, times(1)).toResponse(scopedOrganizationRole);
  }

  @Test
  void getAllUserRolesWillReturnEmptyArrayWhenUserHasNoUserOrganizationRolesReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(scopedRoleRepository.findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(Collections.emptyList());

    List<ScopedRoleResponseDto> allUserRoles =
        scopedRoleService.getAllUserOrganizationRoles(targetUser.getId());

    assertNotNull(allUserRoles);
    assertEquals(0, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(scopedRoleRepository, times(1))
        .findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ);
  }

  @Test
  void getAllUserRolesWillReturnAllRolesThatCurrentUserOrganizationHasPermissionFor() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(scopedRoleRepository.findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(List.of(scopedOrganizationRole));

    List<ScopedRoleResponseDto> allUserRoles =
        scopedRoleService.getAllUserOrganizationRoles(targetUser.getId());

    assertNotNull(allUserRoles);
    assertEquals(1, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(scopedRoleRepository, times(1))
        .findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ);
  }

  @Test
  void getAllUserSystemRolesSuccessfully() {
    List<ScopedRole> systemRoles = List.of(scopedSystemRole);
    when(scopedRoleRepository.findRolesByUserIdAndRoleType(targetUser.getId(), RoleType.SYSTEM))
        .thenReturn(systemRoles);
    when(scopedRoleMapper.toResponseList(systemRoles))
        .thenReturn(List.of(scopedSystemRoleResponseDto));

    List<ScopedRoleResponseDto> rolesResponse =
        scopedRoleService.getAllUserSystemRoles(targetUser.getId());

    assertNotNull(rolesResponse);
    assertEquals(1, rolesResponse.size());
    assertEquals(rolesResponse.getFirst(), scopedSystemRoleResponseDto);

    verify(scopedRoleRepository, times(1))
        .findRolesByUserIdAndRoleType(targetUser.getId(), RoleType.SYSTEM);
    verify(scopedRoleMapper, times(1)).toResponseList(systemRoles);
  }

  @Test
  void getPermissionsByRoleTypeShouldThrowWhenRoleTypeIsNull() {
    assertThrows(
        IllegalArgumentException.class, () -> scopedRoleService.getPermissionsByRoleType(null));
  }

  @Test
  void getPermissionsByRoleTypeSuccessfully() {
    RoleType roleType = RoleType.SYSTEM;

    Set<Permission> expectedPermissions =
        Arrays.stream(Permission.values()).filter(roleType::allows).collect(Collectors.toSet());

    Set<PermissionResponseDto> expectedResponse =
        expectedPermissions.stream()
            .map(
                permission ->
                    new PermissionResponseDto(permission, permission.resolveIncludedPermissions()))
            .collect(Collectors.toSet());

    when(scopedRoleMapper.toPermissionResponseSet(expectedPermissions))
        .thenReturn(expectedResponse);

    Set<PermissionResponseDto> result = scopedRoleService.getPermissionsByRoleType(roleType);

    assertNotNull(result);
    assertEquals(expectedResponse, result);
    assertEquals(expectedPermissions.size(), result.size());

    verify(scopedRoleMapper, times(1)).toPermissionResponseSet(expectedPermissions);
  }

  @Test
  void getCurrentUserSystemPermissionsShouldThrowWhenUserNotAuthenticated() {
    when(authService.getCurrentUser()).thenThrow(NoAuthenticatedUserException.class);

    assertThrows(
        NoAuthenticatedUserException.class,
        () -> scopedRoleService.getCurrentUserSystemPermissions());
  }

  @Test
  void getCurrentUserSystemPermissionsSuccessfully() {
    RoleType roleType = RoleType.SYSTEM;
    Set<Permission> expectedPermissions =
        Arrays.stream(Permission.values()).filter(roleType::allows).collect(Collectors.toSet());

    Set<PermissionResponseDto> expectedResponse =
        expectedPermissions.stream()
            .map(
                permission ->
                    new PermissionResponseDto(permission, permission.resolveIncludedPermissions()))
            .collect(Collectors.toSet());
    when(authService.getCurrentUser()).thenReturn(targetUser);
    when(roleMembershipRepository.findSystemPermissionsByUserId(
            targetUser.getId(), RoleType.SYSTEM))
        .thenReturn(expectedPermissions);
    when(scopedRoleMapper.toPermissionResponseSet(expectedPermissions))
        .thenReturn(expectedResponse);

    Set<PermissionResponseDto> currentUserSystemPermissions =
        scopedRoleService.getCurrentUserSystemPermissions();

    assertNotNull(currentUserSystemPermissions);
    assertEquals(currentUserSystemPermissions, expectedResponse);
    assertEquals(currentUserSystemPermissions.size(), expectedResponse.size());

    verify(authService, times(1)).getCurrentUser();
    verify(roleMembershipRepository, times(1))
        .findSystemPermissionsByUserId(targetUser.getId(), RoleType.SYSTEM);
    verify(scopedRoleMapper, times(1)).toPermissionResponseSet(expectedPermissions);
  }
}
