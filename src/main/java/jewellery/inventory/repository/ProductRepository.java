package jewellery.inventory.repository;

import java.util.List;
import java.util.UUID;
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
}
