package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.resource.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
  boolean existsBySku(String sku);
}
