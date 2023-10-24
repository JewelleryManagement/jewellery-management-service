package jewellery.inventory.aspect;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import jewellery.inventory.aspect.strategy.EntityPayloadStrategy;
import jewellery.inventory.aspect.strategy.PayloadStrategy;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.PreciousMetalResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
  private final SystemEventRepository systemEventRepository;
  private final Map<Class<?>, PayloadStrategy<?, ?>> strategies = new HashMap<>();
  private final JwtUtils jwtUtils;
  private final EntityPayloadStrategy entityPayloadStrategy;

  @PostConstruct
  public void init() {
    strategies.put(UserResponseDto.class, entityPayloadStrategy);
    strategies.put(PreciousMetalResponseDto.class, entityPayloadStrategy);
  }

  public <T, U> void logEvent(EventType type, T entity) {
    PayloadStrategy<T, ?> strategy = (PayloadStrategy<T, ?>) strategies.get(entity.getClass());
    Map<String, Object> payload = strategy.createPayload(entity, null, type);

    SystemEvent event = new SystemEvent();
    event.setExecutorId(jwtUtils.getCurrentUserId());
    event.setType(type);
    event.setTimestamp(Instant.now());
    event.setPayload(payload);

    systemEventRepository.save(event);
  }
}
