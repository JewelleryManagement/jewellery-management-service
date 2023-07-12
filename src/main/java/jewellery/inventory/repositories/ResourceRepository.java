package jewellery.inventory.repositories;

import jewellery.inventory.model.resources.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {}
