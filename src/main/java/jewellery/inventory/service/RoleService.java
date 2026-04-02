package jewellery.inventory.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.RoleRequestDto;
import jewellery.inventory.dto.response.RoleResponseDto;
import jewellery.inventory.exception.not_found.RoleNotFoundException;
import jewellery.inventory.exception.role.RoleAlreadyAssignedException;
import jewellery.inventory.exception.role.RoleNameAlreadyExistsException;
import jewellery.inventory.mapper.RoleMapper;
import jewellery.inventory.model.OrganizationRole;
import jewellery.inventory.model.Permission;
import jewellery.inventory.repository.OrganizationMembershipRepository;
import jewellery.inventory.repository.RoleRepository;
import jewellery.inventory.service.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;
  private final RoleMapper roleMapper;
  private final OrganizationMembershipRepository organizationMembershipRepository;
  private final AuthService authService;

  public RoleResponseDto createRole(RoleRequestDto request) {
    String roleName = request.getName().trim().toUpperCase();

    if (roleRepository.existsByName(roleName)) {
      throw new RoleNameAlreadyExistsException(roleName);
    }

    OrganizationRole role = new OrganizationRole();
    role.setName(roleName);
    role.setPermissions(new HashSet<>(request.getPermissions()));

    return roleMapper.toResponse(roleRepository.save(role));
  }

  @Transactional
  public void deleteRole(UUID roleId) {
    OrganizationRole role = getRoleById(roleId);

    if (organizationMembershipRepository.existsByRoleId(roleId)) {
      throw new RoleAlreadyAssignedException();
    }

    roleRepository.delete(role);
  }

  public RoleResponseDto getRole(UUID roleId) {
    return roleMapper.toResponse(getRoleById(roleId));
  }

  public List<RoleResponseDto> getAllRoles() {
    return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
  }

  public OrganizationRole getRoleByName(String name) {
    return roleRepository.findByName(name).orElseThrow(() -> new RoleNotFoundException(name));
  }

  @Transactional(readOnly = true)
  public List<RoleResponseDto> getAllUserRoles(UUID targetUserId) {
    UUID currentUserId = authService.getCurrentUser().getId();

    return roleRepository
        .findVisibleRolesForUser(
            targetUserId, currentUserId, Permission.ORGANIZATION_USER_ROLES_READ)
        .stream()
        .map(roleMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RoleResponseDto> getAllUserRolesByOrganization(
      UUID targetUserId, UUID organizationId) {
    UUID currentUserId = authService.getCurrentUser().getId();

    return roleRepository
        .findVisibleRolesForUserByOrganization(
            targetUserId, currentUserId, organizationId, Permission.ORGANIZATION_USER_ROLES_READ)
        .stream()
        .map(roleMapper::toResponse)
        .toList();
  }

  private OrganizationRole getRoleById(UUID id) {
    return roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
  }
}
