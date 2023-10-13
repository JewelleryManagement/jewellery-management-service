package jewellery.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SystemEvent {

  @Id @GeneratedValue private UUID id;

  private Instant timestamp;

  @Enumerated(EnumType.STRING)
  private EventType type;

  private UUID executorId;

  //@Column(columnDefinition = "jsonb")
  private String payload;
}
