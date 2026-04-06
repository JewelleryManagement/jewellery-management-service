package jewellery.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jewellery.inventory.model.Permission;
import jewellery.inventory.model.ScopedRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScopedRoleRepository extends JpaRepository<ScopedRole, UUID> {
  boolean existsByName(String name);

  Optional<ScopedRole> findByName(String name);

  @Query(
"""
    SELECT DISTINCT targetMembership.role
    FROM RoleMembership targetMembership
    JOIN RoleMembership currentMembership
        ON currentMembership.organization.id = targetMembership.organization.id
    JOIN currentMembership.role currentRole
    JOIN currentRole.permissions permission
    WHERE targetMembership.user.id = :targetUserId
      AND currentMembership.user.id = :currentUserId
      AND permission = :permission
""")
  List<ScopedRole> findVisibleRolesForUser(
      UUID targetUserId, UUID currentUserId, Permission permission);

  @Query(
"""
    SELECT DISTINCT targetMembership.role
    FROM RoleMembership targetMembership
    JOIN RoleMembership currentMembership
        ON currentMembership.organization.id = targetMembership.organization.id
    JOIN currentMembership.role currentRole
    JOIN currentRole.permissions permission
    WHERE targetMembership.user.id = :targetUserId
      AND targetMembership.organization.id = :organizationId
      AND currentMembership.user.id = :currentUserId
      AND permission = :permission
""")
  List<ScopedRole> findVisibleRolesForUserByOrganization(
      UUID targetUserId, UUID currentUserId, UUID organizationId, Permission permission);
}
