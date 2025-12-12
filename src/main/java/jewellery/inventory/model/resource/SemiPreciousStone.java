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
public class SemiPreciousStone extends Resource {
  private String type;
  private String size;
  private String quality;
  private String color;
  private String shape;
  private String shapeSpecification;
  private String colorHue;
}
