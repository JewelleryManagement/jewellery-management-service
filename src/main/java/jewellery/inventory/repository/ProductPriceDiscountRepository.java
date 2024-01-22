package jewellery.inventory.repository;

import jewellery.inventory.model.ProductPriceDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ProductPriceDiscountRepository extends JpaRepository<ProductPriceDiscount, UUID> {}
