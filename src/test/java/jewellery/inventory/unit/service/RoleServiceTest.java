package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.RoleHelper.createRole;
import static jewellery.inventory.helper.RoleHelper.createRoleRequest;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.request.RoleRequestDto;
import jewellery.inventory.dto.response.RoleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.RoleNotFoundException;
import jewellery.inventory.exception.role.RoleAlreadyAssignedException;
import jewellery.inventory.exception.role.RoleNameAlreadyExistsException;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.mapper.RoleMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.OrganizationRole;
import jewellery.inventory.model.Permission;
import jewellery.inventory.repository.OrganizationMembershipRepository;
import jewellery.inventory.repository.RoleRepository;
import jewellery.inventory.service.RoleService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
  @InjectMocks private RoleService roleService;
  @Mock private RoleRepository roleRepository;
  @Mock private RoleMapper roleMapper;
  @Mock private OrganizationMembershipRepository organizationMembershipRepository;
  @Mock private AuthService authService;

  private RoleRequestDto roleRequestDto;
  private OrganizationRole organizationRole;
  private RoleResponseDto roleResponseDto;
  private UserResponseDto currentUser;
  private UserResponseDto targetUser;

  @BeforeEach
  void setUp() {
    roleRequestDto = createRoleRequest();
    organizationRole = createRole(roleRequestDto);
    roleResponseDto = createRoleResponse(organizationRole);
    currentUser = createTestUserResponseDto(createTestAdminUser());
    targetUser = createTestUserResponseDto(createTestUser());
  }

  @Test
  void createRoleShouldThrowWhenRoleNameAlreadyExists() {
    when(roleRepository.existsByName(roleRequestDto.getName()))
        .thenThrow(RoleNameAlreadyExistsException.class);

    assertThrows(
        RoleNameAlreadyExistsException.class, () -> roleService.createRole(roleRequestDto));
  }

  @Test
  void createRoleSuccessfully() {
    when(roleRepository.existsByName(roleRequestDto.getName())).thenReturn(false);
    when(roleRepository.save(any(OrganizationRole.class))).thenReturn(organizationRole);
    when(roleMapper.toResponse(organizationRole)).thenReturn(roleResponseDto);

    RoleResponseDto roleResponseDto = roleService.createRole(roleRequestDto);

    assertNotNull(roleResponseDto);
    assertEquals(roleResponseDto.getName(), roleRequestDto.getName());
    assertEquals(roleResponseDto.getPermissions(), roleRequestDto.getPermissions());
    verify(roleRepository, times(1)).existsByName(roleRequestDto.getName());
    verify(roleRepository, times(1)).save(any(OrganizationRole.class));
    verify(roleMapper, times(1)).toResponse(organizationRole);
  }

  @Test
  void getRoleByNameShouldThrowWhenRoleWithGivenNameDoesNotExists() {
    when(roleRepository.findByName(roleRequestDto.getName()))
        .thenThrow(RoleNotFoundException.class);

    assertThrows(
        RoleNotFoundException.class, () -> roleService.getRoleByName(roleRequestDto.getName()));
  }

  @Test
  void getRoleByNameSuccessfully() {
    when(roleRepository.findByName(roleRequestDto.getName()))
        .thenReturn(Optional.ofNullable(organizationRole));

    OrganizationRole role = roleService.getRoleByName(roleRequestDto.getName());

    assertNotNull(role);
    assertEquals(role.getName(), roleRequestDto.getName());
    assertEquals(role.getPermissions(), roleRequestDto.getPermissions());
    verify(roleRepository, times(1)).findByName(roleRequestDto.getName());
  }

  @Test
  void deleteRoleShouldThrowWhenRoleDoesNotExists() {
    when(roleRepository.findById(organizationRole.getId())).thenThrow(RoleNotFoundException.class);

    assertThrows(
        RoleNotFoundException.class, () -> roleService.deleteRole(organizationRole.getId()));
  }

  @Test
  void deleteRoleShouldThrowWhenRoleAlreadyAssigned() {
    when(roleRepository.findById(organizationRole.getId()))
        .thenReturn(Optional.ofNullable(organizationRole));
    when(organizationMembershipRepository.existsByRoleId(organizationRole.getId()))
        .thenThrow(RoleAlreadyAssignedException.class);

    assertThrows(
        RoleAlreadyAssignedException.class, () -> roleService.deleteRole(organizationRole.getId()));
  }

  @Test
  void deleteRoleSuccessfully() {
    when(roleRepository.findById(organizationRole.getId()))
        .thenReturn(Optional.ofNullable(organizationRole));
    when(organizationMembershipRepository.existsByRoleId(organizationRole.getId()))
        .thenReturn(false);

    roleService.deleteRole(organizationRole.getId());

    verify(roleRepository, times(1)).findById(organizationRole.getId());
    verify(organizationMembershipRepository, times(1)).existsByRoleId(organizationRole.getId());
  }

  @Test
  void getRoleShouldThrowWhenRoleDoesNotExists() {
    when(roleRepository.findById(organizationRole.getId())).thenThrow(RoleNotFoundException.class);

    assertThrows(RoleNotFoundException.class, () -> roleService.getRole(organizationRole.getId()));
  }

  @Test
  void getRoleSuccessfully() {
    when(roleRepository.findById(organizationRole.getId()))
        .thenReturn(Optional.ofNullable(organizationRole));
    when(roleMapper.toResponse(organizationRole)).thenReturn(roleResponseDto);

    RoleResponseDto role = roleService.getRole(organizationRole.getId());

    assertNotNull(role);
    assertEquals(role.getName(), organizationRole.getName());
    assertEquals(role.getPermissions(), organizationRole.getPermissions());
    verify(roleRepository, times(1)).findById(organizationRole.getId());
  }

  @Test
  void getAllRolesShouldReturnEmptyArrayWhenThereAreNoRoles() {
    when(roleRepository.findAll()).thenReturn(Collections.emptyList());

    List<RoleResponseDto> roles = roleService.getAllRoles();

    assertNotNull(roles);
    assertTrue(roles.isEmpty());
    verify(roleRepository).findAll();
  }

  @Test
  void getAllRolesSuccessfully() {
    when(roleRepository.findAll()).thenReturn(List.of(organizationRole));
    when(roleMapper.toResponse(organizationRole)).thenReturn(roleResponseDto);

    List<RoleResponseDto> roles = roleService.getAllRoles();

    assertNotNull(roles);
    assertEquals(1, roles.size());
    assertEquals(organizationRole.getId(), roles.getFirst().getId());
    assertEquals(organizationRole.getName(), roles.getFirst().getName());
    assertEquals(organizationRole.getPermissions(), roles.getFirst().getPermissions());
    verify(roleRepository, times(1)).findAll();
    verify(roleMapper, times(1)).toResponse(organizationRole);
  }

  @Test
  void getAllUserRolesWillReturnEmptyArrayWhenUserHasNoUserRolesReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleRepository.findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(Collections.emptyList());

    List<RoleResponseDto> allUserRoles = roleService.getAllUserRoles(targetUser.getId());

    assertNotNull(allUserRoles);
    assertEquals(0, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(roleRepository, times(1))
        .findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ);
  }

  @Test
  void getAllUserRolesWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleRepository.findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(List.of(organizationRole));

    List<RoleResponseDto> allUserRoles = roleService.getAllUserRoles(targetUser.getId());

    assertNotNull(allUserRoles);
    assertEquals(1, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(roleRepository, times(1))
        .findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ);
  }

  @Test
  void getAllUserRolesByOrganizationWillReturnEmptyArrayWhenUserHasNoUserRolesReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    Organization organization = OrganizationTestHelper.getTestOrganization();
    when(roleRepository.findVisibleRolesForUserByOrganization(
            targetUser.getId(),
            currentUser.getId(),
            organization.getId(),
            Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(Collections.emptyList());

    List<RoleResponseDto> allUserRoles =
        roleService.getAllUserRolesByOrganization(targetUser.getId(), organization.getId());

    assertNotNull(allUserRoles);
    assertEquals(0, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(roleRepository, times(1))
        .findVisibleRolesForUserByOrganization(
            targetUser.getId(),
            currentUser.getId(),
            organization.getId(),
            Permission.ORGANIZATION_USER_ROLES_READ);
  }

  @Test
  void getAllUserRolesByOrganizationWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    Organization organization = OrganizationTestHelper.getTestOrganization();
    when(roleRepository.findVisibleRolesForUserByOrganization(
            targetUser.getId(),
            currentUser.getId(),
            organization.getId(),
            Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(List.of(organizationRole));

    List<RoleResponseDto> allUserRoles =
        roleService.getAllUserRolesByOrganization(targetUser.getId(), organization.getId());

    assertNotNull(allUserRoles);
    assertEquals(1, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(roleRepository, times(1))
        .findVisibleRolesForUserByOrganization(
            targetUser.getId(),
            currentUser.getId(),
            organization.getId(),
            Permission.ORGANIZATION_USER_ROLES_READ);
  }

  private RoleResponseDto createRoleResponse(OrganizationRole organizationRole) {
    return new RoleResponseDto(
        organizationRole.getId(), organizationRole.getName(), organizationRole.getPermissions());
  }
}
