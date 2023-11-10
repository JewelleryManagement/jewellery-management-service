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
public class PreciousStone extends Resource {
  private String color;
  private Double carat;
  private String cut;
  private String clarity;
  private Double dimensionX;
  private Double dimensionY;
  private Double dimensionZ;
  private String shape;
}
