package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.OrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRoleRepository extends JpaRepository<OrganizationRole, UUID> {
  boolean existsByName(String name);
}
