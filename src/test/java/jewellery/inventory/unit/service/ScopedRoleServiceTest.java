package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ScopedRoleHelper.createRole;
import static jewellery.inventory.helper.ScopedRoleHelper.createRoleRequest;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.RoleNotFoundException;
import jewellery.inventory.exception.role.RoleAlreadyAssignedException;
import jewellery.inventory.exception.role.RoleNameAlreadyExistsException;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.mapper.ScopedRoleMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.ScopedRole;
import jewellery.inventory.repository.OrganizationMembershipRepository;
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
public class ScopedRoleServiceTest {
  @InjectMocks private ScopedRoleService scopedRoleService;
  @Mock private ScopedRoleRepository scopedRoleRepository;
  @Mock private ScopedRoleMapper scopedRoleMapper;
  @Mock private OrganizationMembershipRepository organizationMembershipRepository;
  @Mock private AuthService authService;

  private ScopedRoleRequestDto scopedRoleRequestDto;
  private ScopedRole scopedRole;
  private ScopedRoleResponseDto scopedRoleResponseDto;
  private UserResponseDto currentUser;
  private UserResponseDto targetUser;

  @BeforeEach
  void setUp() {
    scopedRoleRequestDto = createRoleRequest();
    scopedRole = createRole(scopedRoleRequestDto);
    scopedRoleResponseDto = createRoleResponse(scopedRole);
    currentUser = createTestUserResponseDto(createTestAdminUser());
    targetUser = createTestUserResponseDto(createTestUser());
  }

  @Test
  void createRoleShouldThrowWhenRoleNameAlreadyExists() {
    when(scopedRoleRepository.existsByName(scopedRoleRequestDto.getName()))
        .thenThrow(RoleNameAlreadyExistsException.class);

    assertThrows(
        RoleNameAlreadyExistsException.class, () -> scopedRoleService.createRole(scopedRoleRequestDto));
  }

  @Test
  void createRoleSuccessfully() {
    when(scopedRoleRepository.existsByName(scopedRoleRequestDto.getName())).thenReturn(false);
    when(scopedRoleRepository.save(any(ScopedRole.class))).thenReturn(scopedRole);
    when(scopedRoleMapper.toResponse(scopedRole)).thenReturn(this.scopedRoleResponseDto);

    ScopedRoleResponseDto scopedRoleResponseDto = scopedRoleService.createRole(scopedRoleRequestDto);

    assertNotNull(scopedRoleResponseDto);
    assertEquals(scopedRoleResponseDto.getName(), scopedRoleRequestDto.getName());
    assertEquals(scopedRoleResponseDto.getPermissions(), scopedRoleRequestDto.getPermissions());
    verify(scopedRoleRepository, times(1)).existsByName(scopedRoleRequestDto.getName());
    verify(scopedRoleRepository, times(1)).save(any(ScopedRole.class));
    verify(scopedRoleMapper, times(1)).toResponse(scopedRole);
  }

  @Test
  void getRoleByNameShouldThrowWhenRoleWithGivenNameDoesNotExists() {
    when(scopedRoleRepository.findByName(scopedRoleRequestDto.getName()))
        .thenThrow(RoleNotFoundException.class);

    assertThrows(
        RoleNotFoundException.class, () -> scopedRoleService.getRoleByName(scopedRoleRequestDto.getName()));
  }

  @Test
  void getRoleByNameSuccessfully() {
    when(scopedRoleRepository.findByName(scopedRoleRequestDto.getName()))
        .thenReturn(Optional.ofNullable(scopedRole));

    ScopedRole role = scopedRoleService.getRoleByName(scopedRoleRequestDto.getName());

    assertNotNull(role);
    assertEquals(role.getName(), scopedRoleRequestDto.getName());
    assertEquals(role.getPermissions(), scopedRoleRequestDto.getPermissions());
    verify(scopedRoleRepository, times(1)).findByName(scopedRoleRequestDto.getName());
  }

  @Test
  void deleteRoleShouldThrowWhenRoleDoesNotExists() {
    when(scopedRoleRepository.findById(scopedRole.getId())).thenThrow(RoleNotFoundException.class);

    assertThrows(
        RoleNotFoundException.class, () -> scopedRoleService.deleteRole(scopedRole.getId()));
  }

  @Test
  void deleteRoleShouldThrowWhenRoleAlreadyAssigned() {
    when(scopedRoleRepository.findById(scopedRole.getId()))
        .thenReturn(Optional.ofNullable(scopedRole));
    when(organizationMembershipRepository.existsByRoleId(scopedRole.getId()))
        .thenThrow(RoleAlreadyAssignedException.class);

    assertThrows(
        RoleAlreadyAssignedException.class, () -> scopedRoleService.deleteRole(scopedRole.getId()));
  }

  @Test
  void deleteRoleSuccessfully() {
    when(scopedRoleRepository.findById(scopedRole.getId()))
        .thenReturn(Optional.ofNullable(scopedRole));
    when(organizationMembershipRepository.existsByRoleId(scopedRole.getId()))
        .thenReturn(false);

    scopedRoleService.deleteRole(scopedRole.getId());

    verify(scopedRoleRepository, times(1)).findById(scopedRole.getId());
    verify(organizationMembershipRepository, times(1)).existsByRoleId(scopedRole.getId());
  }

  @Test
  void getRoleShouldThrowWhenRoleDoesNotExists() {
    when(scopedRoleRepository.findById(scopedRole.getId())).thenThrow(RoleNotFoundException.class);

    assertThrows(RoleNotFoundException.class, () -> scopedRoleService.getRole(scopedRole.getId()));
  }

  @Test
  void getRoleSuccessfully() {
    when(scopedRoleRepository.findById(scopedRole.getId()))
        .thenReturn(Optional.ofNullable(scopedRole));
    when(scopedRoleMapper.toResponse(scopedRole)).thenReturn(scopedRoleResponseDto);

    ScopedRoleResponseDto role = scopedRoleService.getRole(scopedRole.getId());

    assertNotNull(role);
    assertEquals(role.getName(), scopedRole.getName());
    assertEquals(role.getPermissions(), scopedRole.getPermissions());
    verify(scopedRoleRepository, times(1)).findById(scopedRole.getId());
  }

  @Test
  void getAllRolesShouldReturnEmptyArrayWhenThereAreNoRoles() {
    when(scopedRoleRepository.findAll()).thenReturn(Collections.emptyList());

    List<ScopedRoleResponseDto> roles = scopedRoleService.getAllRoles();

    assertNotNull(roles);
    assertTrue(roles.isEmpty());
    verify(scopedRoleRepository).findAll();
  }

  @Test
  void getAllRolesSuccessfully() {
    when(scopedRoleRepository.findAll()).thenReturn(List.of(scopedRole));
    when(scopedRoleMapper.toResponse(scopedRole)).thenReturn(scopedRoleResponseDto);

    List<ScopedRoleResponseDto> roles = scopedRoleService.getAllRoles();

    assertNotNull(roles);
    assertEquals(1, roles.size());
    assertEquals(scopedRole.getId(), roles.getFirst().getId());
    assertEquals(scopedRole.getName(), roles.getFirst().getName());
    assertEquals(scopedRole.getPermissions(), roles.getFirst().getPermissions());
    verify(scopedRoleRepository, times(1)).findAll();
    verify(scopedRoleMapper, times(1)).toResponse(scopedRole);
  }

  @Test
  void getAllUserRolesWillReturnEmptyArrayWhenUserHasNoUserRolesReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(scopedRoleRepository.findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(Collections.emptyList());

    List<ScopedRoleResponseDto> allUserRoles = scopedRoleService.getAllUserRoles(targetUser.getId());

    assertNotNull(allUserRoles);
    assertEquals(0, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(scopedRoleRepository, times(1))
        .findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ);
  }

  @Test
  void getAllUserRolesWillReturnAllRolesThatCurrentUserHasPermissionFor() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(scopedRoleRepository.findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(List.of(scopedRole));

    List<ScopedRoleResponseDto> allUserRoles = scopedRoleService.getAllUserRoles(targetUser.getId());

    assertNotNull(allUserRoles);
    assertEquals(1, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(scopedRoleRepository, times(1))
        .findVisibleRolesForUser(
            targetUser.getId(), currentUser.getId(), Permission.ORGANIZATION_USER_ROLES_READ);
  }

  @Test
  void getAllUserRolesByOrganizationWillReturnEmptyArrayWhenUserHasNoUserRolesReadPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    Organization organization = OrganizationTestHelper.getTestOrganization();
    when(scopedRoleRepository.findVisibleRolesForUserByOrganization(
            targetUser.getId(),
            currentUser.getId(),
            organization.getId(),
            Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(Collections.emptyList());

    List<ScopedRoleResponseDto> allUserRoles =
        scopedRoleService.getAllUserRolesByOrganization(targetUser.getId(), organization.getId());

    assertNotNull(allUserRoles);
    assertEquals(0, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(scopedRoleRepository, times(1))
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
    when(scopedRoleRepository.findVisibleRolesForUserByOrganization(
            targetUser.getId(),
            currentUser.getId(),
            organization.getId(),
            Permission.ORGANIZATION_USER_ROLES_READ))
        .thenReturn(List.of(scopedRole));

    List<ScopedRoleResponseDto> allUserRoles =
        scopedRoleService.getAllUserRolesByOrganization(targetUser.getId(), organization.getId());

    assertNotNull(allUserRoles);
    assertEquals(1, allUserRoles.size());
    verify(authService, times(1)).getCurrentUser();
    verify(scopedRoleRepository, times(1))
        .findVisibleRolesForUserByOrganization(
            targetUser.getId(),
            currentUser.getId(),
            organization.getId(),
            Permission.ORGANIZATION_USER_ROLES_READ);
  }

  private ScopedRoleResponseDto createRoleResponse(ScopedRole scopedRole) {
    return new ScopedRoleResponseDto(
        scopedRole.getId(), scopedRole.getName(), scopedRole.getPermissions());
  }
}
