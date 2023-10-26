package jewellery.inventory.aspect;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.aspect.strategy.EntityPayloadStrategy;
import jewellery.inventory.aspect.strategy.PayloadStrategy;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.dto.response.resource.LinkingPartResponseDto;
import jewellery.inventory.dto.response.resource.PearlResponseDto;
import jewellery.inventory.dto.response.resource.PreciousMetalResponseDto;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.SystemEvent;
import jewellery.inventory.repository.SystemEventRepository;
import jewellery.inventory.security.JwtUtils;
import jewellery.inventory.service.ResourceService;
import jewellery.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

  private final SystemEventRepository systemEventRepository;
  private final Map<Class<?>, PayloadStrategy<?, ?>> strategies = new HashMap<>();
  private final JwtUtils jwtUtils;
  private final EntityPayloadStrategy entityPayloadStrategy;
  private final UserService userService;
  private final ResourceService resourceService;

  @PostConstruct
  public void init() {
    strategies.put(UserResponseDto.class, entityPayloadStrategy);
    strategies.put(PreciousMetalResponseDto.class, entityPayloadStrategy);
    strategies.put(PearlResponseDto.class, entityPayloadStrategy);
    strategies.put(GemstoneResponseDto.class, entityPayloadStrategy);
    strategies.put(LinkingPartResponseDto.class, entityPayloadStrategy);
  }

  public <T, U> void logEvent(EventType type, T entity, @Nullable U oldEntity) throws Exception {
    Class<?> dtoClass = entityPayloadStrategy.getDtoClass(entity.getClass());

    PayloadStrategy<T, ?> strategy = (PayloadStrategy<T, ?>) strategies.get(dtoClass);
    Map<String, Object> payload;

    if (type.equals(EventType.ENTITY_UPDATE)) {
      payload = strategy.createUpdatePayload(entity, oldEntity, type);
    } else {
      payload = strategy.createPayload(entity, type);
    }

    SystemEvent event = new SystemEvent();
    event.setExecutorId(jwtUtils.getCurrentUserId());
    event.setType(type);
    event.setTimestamp(Instant.now());
    event.setPayload(payload);

    systemEventRepository.save(event);
  }

  public Object fetchEntityByIdAsDto(UUID id, Class<?> entityType) {
    if (entityType.equals(UserRequestDto.class)) {
      return userService.fetchByIdAsDto(id);
    } else if (ResourceRequestDto.class.isAssignableFrom(entityType)) {
      return resourceService.fetchByIdAsDto(id);
    }
    throw new IllegalArgumentException("Unsupported entity type: " + entityType);
  }
}
