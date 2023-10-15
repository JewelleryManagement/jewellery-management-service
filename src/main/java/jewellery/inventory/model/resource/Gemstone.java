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
public class Gemstone extends Resource {
  private String color;
  private double carat;
  private String cut;
  private String clarity;
  private String size;
  private double dimensionX;
  private double dimensionY;
  private double dimensionZ;
  private String shape;
}
