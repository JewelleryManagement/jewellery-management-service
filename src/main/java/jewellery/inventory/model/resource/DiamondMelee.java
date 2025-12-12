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
public class DiamondMelee extends Resource {
  private String color;
  private String cut;
  private String clarity;
  private String shape;
  private String size;
  private String type;
  private BigDecimal carat;
}
