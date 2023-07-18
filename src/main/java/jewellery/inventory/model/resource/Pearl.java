package jewellery.inventory.model.resource;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Pearl extends Resource {
    private String type;
    private double size;
    private String quality;
    private String color;
    private String shape;
}
