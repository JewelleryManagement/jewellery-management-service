package jewellery.inventory.service;

import java.util.List;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemEventService {

  private SystemEventRepository repository;

  public SystemEvent createEvent(SystemEvent event) {
    return repository.save(event);
  }

  public List<SystemEvent> getAllEvents() {
    return repository.findAll();
  }
}
