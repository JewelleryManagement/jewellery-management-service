package jewellery.inventory.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.model.ResourceInUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceInUserRepository extends JpaRepository<ResourceInUser, UUID> {
  @Query(
      "SELECT COALESCE(SUM(riu.quantity), 0.0) FROM ResourceInUser riu WHERE riu.resource.id = :resourceId")
  BigDecimal sumQuantityByResource(@Param("resourceId") UUID resourceId);
  Optional<ResourceInUser> findByResourceIdAndOwnerId(UUID resourceId, UUID ownerId);
}
