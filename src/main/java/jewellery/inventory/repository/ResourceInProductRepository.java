package jewellery.inventory.repository;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.model.resource.ResourceInProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceInProductRepository extends JpaRepository<ResourceInProduct, UUID> {
  Optional<ResourceInProduct> findByResourceIdAndProductId(UUID resourceId, UUID productId);

  boolean existsByResourceId(UUID id);
}
