package jewellery.inventory.service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRoleRequest;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.OrganizationMembership;
import jewellery.inventory.model.OrganizationRole;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.OrganizationMembershipRepository;
import jewellery.inventory.repository.OrganizationRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationRoleService {
  private final OrganizationService organizationService;
  private final OrganizationRoleRepository organizationRoleRepository;
  private final OrganizationMembershipRepository organizationMembershipRepository;
  private final UserService userService;

  public OrganizationRole createRole(OrganizationRoleRequest request) {
    String roleName = request.getName().trim().toUpperCase();

    if (organizationRoleRepository.existsByName(roleName)) {
      throw new IllegalArgumentException("Role with this name already exists");
    }

    if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
      throw new IllegalArgumentException("Role must have at least one permission");
    }

    OrganizationRole role = new OrganizationRole();
    role.setName(roleName);
    role.setPermissions(new HashSet<>(request.getPermissions()));

    return organizationRoleRepository.save(role);
  }

  @Transactional
  public void assignRoleToUserInOrganization(UUID userId, UUID organizationId, UUID roleId) {

    OrganizationMembership membership =
        organizationMembershipRepository
            .findByUserIdAndOrganizationId(userId, organizationId)
            .orElseGet(() -> createMembership(userId, organizationId));

    OrganizationRole role =
        organizationRoleRepository
            .findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));

    boolean alreadyAssigned =
        membership.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));

    if (alreadyAssigned) {
      throw new IllegalArgumentException("Role already assigned");
    }

    membership.getRoles().add(role);
  }

  private OrganizationMembership createMembership(UUID userId, UUID organizationId) {
    User user = userService.getUser(userId);

    Organization organization = organizationService.getOrganization(organizationId);

    OrganizationMembership membership = new OrganizationMembership();
    membership.setUser(user);
    membership.setOrganization(organization);

    return organizationMembershipRepository.save(membership);
  }
}
