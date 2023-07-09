package jewellery.inventory.repositories;

import jewellery.inventory.model.resources.Gemstone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GemstoneRepository extends JpaRepository<Gemstone, Long> {
}
