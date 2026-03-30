package jewellery.inventory.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.ResourceInOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceInOrganizationRepository
    extends JpaRepository<ResourceInOrganization, UUID> {
  @Query(
      "SELECT COALESCE(SUM(rio.quantity), 0.0) FROM ResourceInOrganization rio WHERE rio.resource.id = :resourceId")
  BigDecimal sumQuantityByResource(@Param("resourceId") UUID resourceId);

  boolean existsByResourceId(UUID id);

  @Query(
"""
    select rio
    from ResourceInOrganization rio
    join OrganizationMembership m on m.organization.id = rio.organization.id
    join m.roles r
    join r.permissions p
    where rio.resource.id = :resourceId
      and m.user.id = :userId
      and p = :permission
""")
  List<ResourceInOrganization> findAllByResourceIdAndUserIdAndPermission(
      UUID resourceId, UUID userId, Permission permission);
}
