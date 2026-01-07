package jewellery.inventory.repository;

import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.model.ResourceInOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceInOrganizationRepository
    extends JpaRepository<ResourceInOrganization, UUID> {
  @Query(
      "SELECT COALESCE(SUM(rio.quantity), 0.0) FROM ResourceInOrganization rio WHERE rio.resource.id = :resourceId")
  BigDecimal sumQuantityByResource(@Param("resourceId") UUID resourceId);
}
