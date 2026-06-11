package jewellery.inventory.service;

import java.util.*;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.PermissionResponseDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.exception.not_found.RoleNotFoundException;
import jewellery.inventory.exception.role.RoleAlreadyAssignedException;
import jewellery.inventory.exception.role.RoleNameAlreadyExistsException;
import jewellery.inventory.mapper.ScopedRoleMapper;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleType;
import jewellery.inventory.model.ScopedRole;
import jewellery.inventory.repository.RoleMembershipRepository;
import jewellery.inventory.repository.ScopedRoleRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScopedRoleService {
  private final ScopedRoleRepository scopedRoleRepository;
  private final ScopedRoleMapper scopedRoleMapper;
  private final RoleMembershipRepository roleMembershipRepository;
  private final AuthService authService;

  public ScopedRoleResponseDto createRole(ScopedRoleRequestDto request) {
    String roleName = request.getName().trim().toUpperCase();

    if (scopedRoleRepository.existsByName(roleName)) {
      throw new RoleNameAlreadyExistsException(roleName);
    }

    if (request.getRoleType() == null) {
      throw new IllegalArgumentException("Role type must not be null");
    }

    Set<Permission> requestedPermissions =
        request.getPermissions() == null ? Set.of() : new HashSet<>(request.getPermissions());

    ScopedRole role = new ScopedRole();
    role.setName(roleName);
    role.setRoleType(request.getRoleType());
    role.setPermissions(Permission.resolveAll(requestedPermissions));

    return scopedRoleMapper.toResponse(scopedRoleRepository.save(role));
  }

  @Transactional
  public void deleteRole(UUID roleId) {
    ScopedRole role = getRoleById(roleId);

    if (roleMembershipRepository.existsByRoleId(roleId)) {
      throw new RoleAlreadyAssignedException();
    }

    scopedRoleRepository.delete(role);
  }

  public ScopedRoleResponseDto getRole(UUID roleId) {
    return scopedRoleMapper.toResponse(getRoleById(roleId));
  }

  public ScopedRole getRoleByName(String name) {
    return scopedRoleRepository.findByName(name).orElseThrow(() -> new RoleNotFoundException(name));
  }

  public List<ScopedRoleResponseDto> getRolesByType(RoleType roleType) {
    return scopedRoleRepository.findByRoleType(roleType).stream()
        .map(scopedRoleMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ScopedRoleResponseDto> getAllUserOrganizationRoles(UUID targetUserId) {
    UUID currentUserId = authService.getCurrentUser().getId();

    return scopedRoleRepository
        .findVisibleRolesForUser(
            targetUserId, currentUserId, Permission.ORGANIZATION_USER_ROLES_READ)
        .stream()
        .map(scopedRoleMapper::toResponse)
        .toList();
  }

  public List<ScopedRoleResponseDto> getAllUserSystemRoles(UUID userId) {
    return scopedRoleMapper.toResponseList(
        scopedRoleRepository.findRolesByUserIdAndRoleType(userId, RoleType.SYSTEM));
  }

  public Set<PermissionResponseDto> getPermissionsByRoleType(RoleType roleType) {
    if (roleType == null) {
      throw new IllegalArgumentException("Role type must not be null");
    }

    Set<Permission> permissions =
        Arrays.stream(Permission.values()).filter(roleType::allows).collect(Collectors.toSet());

    return scopedRoleMapper.toPermissionResponseSet(permissions);
  }

  public Set<PermissionResponseDto> getCurrentUserSystemPermissions() {
    UUID currentUserId = authService.getCurrentUser().getId();

    Set<Permission> permissions =
        roleMembershipRepository.findSystemPermissionsByUserId(currentUserId, RoleType.SYSTEM);

    return scopedRoleMapper.toPermissionResponseSet(permissions);
  }

  private ScopedRole getRoleById(UUID id) {
    return scopedRoleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
  }
}
