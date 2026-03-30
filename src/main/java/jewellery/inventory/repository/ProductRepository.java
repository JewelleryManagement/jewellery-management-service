package jewellery.inventory.repository;

import java.util.List;
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
    join OrganizationMembership m on m.organization.id = p.organization.id
    join m.roles role
    join role.permissions perm
    where rip.resource.id = :resourceId
      and m.user.id = :userId
      and perm = :permission
""")
  List<Product> findAllByResourceIdAndUserIdAndPermission(
      UUID resourceId, UUID userId, Permission permission);
}
