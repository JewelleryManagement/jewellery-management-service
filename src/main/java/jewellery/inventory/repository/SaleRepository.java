package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {}
