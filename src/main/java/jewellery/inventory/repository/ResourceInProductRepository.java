package jewellery.inventory.repository;

import jewellery.inventory.model.resource.ResourceInProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceInProductRepository extends JpaRepository<ResourceInProduct, UUID> {
}
