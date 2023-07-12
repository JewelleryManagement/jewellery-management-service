package jewellery.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class SystemEventDTO {
  private UUID id;
  private String data;
  private LocalDateTime timestamp;
  private String type;
}
