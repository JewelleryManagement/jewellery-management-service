package jewellery.inventory.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.exception.not_found.RoleNotFoundException;
import jewellery.inventory.exception.role.RoleAlreadyAssignedException;
import jewellery.inventory.exception.role.RoleNameAlreadyExistsException;
import jewellery.inventory.mapper.ScopedRoleMapper;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.ScopedRole;
import jewellery.inventory.repository.OrganizationMembershipRepository;
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
  private final OrganizationMembershipRepository organizationMembershipRepository;
  private final AuthService authService;

  public ScopedRoleResponseDto createRole(ScopedRoleRequestDto request) {
    String roleName = request.getName().trim().toUpperCase();

    if (scopedRoleRepository.existsByName(roleName)) {
      throw new RoleNameAlreadyExistsException(roleName);
    }

    ScopedRole role = new ScopedRole();
    role.setName(roleName);
    role.setPermissions(new HashSet<>(request.getPermissions()));

    return scopedRoleMapper.toResponse(scopedRoleRepository.save(role));
  }

  @Transactional
  public void deleteRole(UUID roleId) {
    ScopedRole role = getRoleById(roleId);

    if (organizationMembershipRepository.existsByRoleId(roleId)) {
      throw new RoleAlreadyAssignedException();
    }

    scopedRoleRepository.delete(role);
  }

  public ScopedRoleResponseDto getRole(UUID roleId) {
    return scopedRoleMapper.toResponse(getRoleById(roleId));
  }

  public List<ScopedRoleResponseDto> getAllRoles() {
    return scopedRoleRepository.findAll().stream().map(scopedRoleMapper::toResponse).toList();
  }

  public ScopedRole getRoleByName(String name) {
    return scopedRoleRepository.findByName(name).orElseThrow(() -> new RoleNotFoundException(name));
  }

  @Transactional(readOnly = true)
  public List<ScopedRoleResponseDto> getAllUserRoles(UUID targetUserId) {
    UUID currentUserId = authService.getCurrentUser().getId();

    return scopedRoleRepository
        .findVisibleRolesForUser(
            targetUserId, currentUserId, Permission.ORGANIZATION_USER_ROLES_READ)
        .stream()
        .map(scopedRoleMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ScopedRoleResponseDto> getAllUserRolesByOrganization(
      UUID targetUserId, UUID organizationId) {
    UUID currentUserId = authService.getCurrentUser().getId();

    return scopedRoleRepository
        .findVisibleRolesForUserByOrganization(
            targetUserId, currentUserId, organizationId, Permission.ORGANIZATION_USER_ROLES_READ)
        .stream()
        .map(scopedRoleMapper::toResponse)
        .toList();
  }

  private ScopedRole getRoleById(UUID id) {
    return scopedRoleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
  }
}
