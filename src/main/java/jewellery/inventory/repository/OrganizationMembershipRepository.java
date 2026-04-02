package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationMembershipRepository extends JpaRepository<RoleMembership, UUID> {

  @Query(
      """
      select count(m) > 0
      from RoleMembership m
      join m.role r
      join r.permissions perm
      where m.user.id = :userId
        and m.organization.id = :organizationId
        and perm = :permission
      """)
  boolean hasPermissionInOrganization(UUID userId, UUID organizationId, Permission permission);

  boolean existsByUserIdAndOrganizationIdAndRoleId(UUID userId, UUID organizationId, UUID roleId);

  @Query(
      """
      select count(m) > 0
      from RoleMembership m
      join m.role r
      join r.permissions perm
      join Product p on p.organization.id = m.organization.id
      where p.id = :productId
        and m.user.id = :userId
        and perm = :permission
      """)
  boolean hasAccessToProduct(UUID productId, UUID userId, Permission permission);

  @Query(
      """
      select count(m) > 0
      from RoleMembership m
      join m.role r
      join r.permissions perm
      join Sale s on s.organizationSeller.id = m.organization.id
      where s.id = :saleId
        and m.user.id = :userId
        and perm = :permission
      """)
  boolean hasAccessToSale(UUID saleId, UUID userId, Permission permission);

  boolean existsByRoleId(UUID roleId);
}
