package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

}
