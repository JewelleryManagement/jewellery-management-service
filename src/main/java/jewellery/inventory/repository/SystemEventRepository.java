package jewellery.inventory.repository;

import jewellery.inventory.model.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemEventRepository extends JpaRepository<SystemEvent, Long> {}
