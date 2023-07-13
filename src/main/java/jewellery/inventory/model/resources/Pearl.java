package jewellery.inventory.model.resources;

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
