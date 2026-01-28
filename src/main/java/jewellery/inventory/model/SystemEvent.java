package jewellery.inventory.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
public class SystemEvent {

  @Id @GeneratedValue private UUID id;

  private Instant timestamp;

  @Enumerated(EnumType.STRING)
  private EventType type;

  @Column(columnDefinition = "jsonb")
  @Type(JsonBinaryType.class)
  private Map<String, Object> executor = new HashMap<>();

  @Column(columnDefinition = "jsonb")
  @Type(JsonBinaryType.class)
  private Map<String, Object> payload = new HashMap<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "system_event_related", joinColumns = @JoinColumn(name = "event_id"))
  @Column(name = "related_id", nullable = false)
  private Set<UUID> relatedIds = new HashSet<>();
}
