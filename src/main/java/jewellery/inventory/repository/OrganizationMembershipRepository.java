package jewellery.inventory.repository;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.model.OrganizationMembership;
import jewellery.inventory.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationMembershipRepository
    extends JpaRepository<OrganizationMembership, UUID> {

  @Query(
"""
    select distinct m
    from OrganizationMembership m
    left join fetch m.roles r
    left join fetch r.permissions
    where m.user.id = :userId
      and m.organization.id = :organizationId
""")
  Optional<OrganizationMembership> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

  @Query(
"""
    select count(m) > 0
    from OrganizationMembership m
    join m.roles r
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
    from OrganizationMembership m
    join m.roles r
    join r.permissions perm
    join Sale s on s.organizationSeller.id = m.organization.id
    where s.id = :saleId
      and m.user.id = :userId
      and perm = :permission
""")
  boolean hasAccessToSale(UUID saleId, UUID userId, Permission permission);
}
