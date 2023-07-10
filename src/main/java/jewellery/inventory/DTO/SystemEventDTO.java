package jewellery.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemEventDTO {
    private UUID id;

    @Column(columnDefinition = "jsonb")
    private String data;

    private LocalDateTime timestamp;
    private String type;
}

