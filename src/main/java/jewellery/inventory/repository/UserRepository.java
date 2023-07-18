package jewellery.inventory.repository;

import jewellery.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
