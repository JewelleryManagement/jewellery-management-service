package jewellery.inventory.aspect.strategy;

import java.util.Map;
import jewellery.inventory.model.EventType;

public class EntityUpdatePayloadStrategy extends PayloadStrategy<Object, Object> {
  @Override
  public Map<String, Object> createPayload(
      Object entity, Object updatedEntity, EventType eventType) {
    return null;
  }
}
