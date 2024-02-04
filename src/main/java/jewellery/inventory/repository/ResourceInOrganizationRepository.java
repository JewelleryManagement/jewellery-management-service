package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.ResourceInOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceInOrganizationRepository extends JpaRepository<ResourceInOrganization, UUID> {

}
