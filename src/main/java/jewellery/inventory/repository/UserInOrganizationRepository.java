package jewellery.inventory.repository;

import java.util.UUID;
import jewellery.inventory.model.UserInOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInOrganizationRepository extends JpaRepository<UserInOrganization, UUID> {

}
