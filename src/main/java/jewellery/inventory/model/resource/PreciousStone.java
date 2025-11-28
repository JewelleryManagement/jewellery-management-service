package jewellery.inventory.model.resource;

import jakarta.persistence.Entity;
import java.math.BigDecimal;
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
  private BigDecimal carat;
  private String cut;
  private String clarity;
  private BigDecimal dimensionX;
  private BigDecimal dimensionY;
  private BigDecimal dimensionZ;
  private String shape;
  private String type;
  private String colorHue;
  private String polish;
  private String symmetry;
  private String fluorescence;
  private String certificate;
}
