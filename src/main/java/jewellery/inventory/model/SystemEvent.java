package jewellery.inventory.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class SystemEvent {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(columnDefinition = "jsonb")
    private String data;

    private LocalDateTime timestamp;
    private String type;
}
