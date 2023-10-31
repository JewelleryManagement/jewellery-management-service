package jewellery.inventory.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
public class SystemEvent {

  @Id @GeneratedValue private UUID id;

  private String timestamp;

  @Enumerated(EnumType.STRING)
  private EventType type;

  @Column(columnDefinition = "jsonb")
  @Type(JsonBinaryType.class)
  private Map<String, Object> executor = new HashMap<>();

  @Column(columnDefinition = "jsonb")
  @Type(JsonBinaryType.class)
  private Map<String, Object> payload = new HashMap<>();
}
