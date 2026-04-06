package jewellery.inventory.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
  List<Product> findAllByOwnerId(UUID ownerId);

  @Query(
"""
  SELECT DISTINCT p
  FROM Product p
  JOIN p.resourcesContent ric
  WHERE ric.resource.id = :resourceId
""")
  List<Product> findAllByResourceId(@Param("resourceId") UUID resourceId);

  @Query(
"""
    select distinct p
    from Product p
    join p.resourcesContent rip
    join RoleMembership m
      on m.organization = p.organization
    join m.role role
    join role.permissions perm
    where rip.resource.id = :resourceId
      and m.user.id = :userId
      and perm = :permission
""")
  List<Product> findAllByResourceIdAndUserIdAndPermission(
      UUID resourceId, UUID userId, Permission permission);

  @Query(
"""
    select distinct p
    from Product p
    join Sale s on s.organizationSeller = p.organization
    join s.products ppd
    join ppd.product saleProduct
    join RoleMembership m on m.organization = p.organization
    join m.role r
    join r.permissions perm
    where p.owner.id = :ownerId
      and saleProduct = p
      and s.organizationSeller is not null
      and m.user.id = :currentUserId
      and perm in :permissions
    group by p
    having count(distinct perm) = :permissionCount
""")
  List<Product> findAllReadableByOwnerId(
      UUID ownerId, UUID currentUserId, Set<Permission> permissions, long permissionCount);
}
