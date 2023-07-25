package jewellery.inventory.repository;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByName(String name);

  Optional<User> findByEmail(String email);
}
