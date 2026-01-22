package jewellery.inventory.repository;

import java.util.List;
import java.util.UUID;
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
}
