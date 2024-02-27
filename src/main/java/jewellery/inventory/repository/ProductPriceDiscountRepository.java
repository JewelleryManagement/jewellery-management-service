package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.ProductPriceDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPriceDiscountRepository extends JpaRepository<ProductPriceDiscount, UUID> {
}
