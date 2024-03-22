package jewellery.inventory.repository;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.model.UserInOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInOrganizationRepository extends JpaRepository<UserInOrganization, UUID> {
  Optional<UserInOrganization> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
