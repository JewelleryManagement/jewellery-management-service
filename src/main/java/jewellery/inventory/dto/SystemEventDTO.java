package jewellery.inventory.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SystemEventDTO {
    private UUID id;
    private String data;
    private LocalDateTime timestamp;
    private String type;
}
