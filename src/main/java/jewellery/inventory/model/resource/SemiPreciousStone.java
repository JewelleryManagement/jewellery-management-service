package jewellery.inventory.model.resource;

import jakarta.persistence.Entity;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class SemiPreciousStone extends Resource {
    private String color;
    private String cut;
    private String clarity;
    private String shape;
    private BigDecimal size;
}
