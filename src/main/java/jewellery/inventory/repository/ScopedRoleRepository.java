package jewellery.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleType;
import jewellery.inventory.model.ScopedRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  List<ScopedRole> findByRoleType(RoleType roleType);

  @Query(
      """
            select distinct r
            from RoleMembership m
            join m.role r
            where m.user.id = :userId
              and r.roleType = :roleType
          """)
  List<ScopedRole> findRolesByUserIdAndRoleType(
      @Param("userId") UUID userId, @Param("roleType") RoleType roleType);
}
