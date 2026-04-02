package jewellery.inventory.repository;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
  @Query(
"""
    SELECT DISTINCT s
    FROM Sale s
    JOIN s.resources pri
    WHERE pri.resource.id = :resourceId
""")
  List<Sale> findAllByResourceId(@Param("resourceId") UUID resourceId);

  @Query(
"""
    select distinct s
    from Sale s
    join RoleMembership m on m.organization = s.organizationSeller
    join m.role r
    join r.permissions p
    where s.organizationSeller is not null
      and m.user.id = :userId
      and p = :permission
""")
  List<Sale> findAllByUserIdAndPermission(UUID userId, Permission permission);

  @Query(
"""
    select distinct s
    from Sale s
    join s.resources pru
    join RoleMembership m on m.organization = s.organizationSeller
    join m.role r
    join r.permissions p
    where pru.resource.id = :resourceId
      and s.organizationSeller is not null
      and m.user.id = :userId
      and p = :permission
""")
  List<Sale> findAllByResourceIdAndUserIdAndPermission(
      UUID resourceId, UUID userId, Permission permission);
}
