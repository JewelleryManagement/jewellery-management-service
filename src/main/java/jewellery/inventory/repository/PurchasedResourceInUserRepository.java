package jewellery.inventory.repository;

import jewellery.inventory.model.PurchasedResourceInUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchasedResourceInUserRepository extends JpaRepository<PurchasedResourceInUser, UUID> {
    Optional<PurchasedResourceInUser> findByResourceIdAndPartOfSaleId(UUID resourceId, UUID saleId);
}
