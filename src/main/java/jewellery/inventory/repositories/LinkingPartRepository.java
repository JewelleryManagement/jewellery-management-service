package jewellery.inventory.repositories;

import jewellery.inventory.model.resources.LinkingPart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkingPartRepository extends JpaRepository<LinkingPart, Long> {
        }
