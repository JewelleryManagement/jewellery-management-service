package jewellery.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.PurchasedResourceInUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasedResourceInUserRepository
    extends JpaRepository<PurchasedResourceInUser, UUID> {
  Optional<PurchasedResourceInUser> findByResourceIdAndPartOfSaleId(UUID resourceId, UUID saleId);

  List<PurchasedResourceInUser> findAllByOwnerId(UUID ownerId);

  @Query(
"""
    select pru
    from PurchasedResourceInUser pru
    join pru.partOfSale s
    join OrganizationMembership m on m.organization.id = s.organizationSeller.id
    join m.roles r
    join r.permissions p
    where pru.owner.id = :targetUserId
      and m.user.id = :currentUserId
      and s.organizationSeller is not null
      and p in :permissions
    group by pru
    having count(distinct p) = :permissionCount
""")
  List<PurchasedResourceInUser> findAllByOwnerIdAndAllPermissions(
      UUID targetUserId, UUID currentUserId, Set<Permission> permissions, long permissionCount);
}
