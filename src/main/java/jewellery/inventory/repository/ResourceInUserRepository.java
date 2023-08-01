package jewellery.inventory.repository;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.model.resource.ResourceInUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceInUserRepository extends JpaRepository<ResourceInUser, UUID> {
  }
