package jewellery.inventory.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import jewellery.inventory.model.EventType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class SystemEventResponseDto {
  private UUID id;
  private Instant timestamp;
  private EventType type;
  private Map<String, Object> executor;
  private Map<String, Object> payload;

  public SystemEventResponseDto(
      UUID id, Instant timestamp, EventType type, Object executor, Object payload) {
    this.id = id;
    this.timestamp = timestamp;
    this.type = type;
    this.executor = (Map<String, Object>) executor;
    this.payload = (Map<String, Object>) payload;
  }
}
