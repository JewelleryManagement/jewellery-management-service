package jewellery.inventory.repositories;

import jewellery.inventory.model.resources.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {}
