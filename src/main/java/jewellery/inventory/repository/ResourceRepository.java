package jewellery.inventory.repository;

import jewellery.inventory.model.resource.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

}
