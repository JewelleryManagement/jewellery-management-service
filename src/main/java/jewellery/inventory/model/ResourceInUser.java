package jewellery.inventory.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.model.resource.Resource;
import lombok.*;

@Entity
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceInUser {
  @Id @GeneratedValue private UUID id;

  @ManyToOne private User owner;

  @ManyToOne private Resource resource;

  private BigDecimal quantity;

  @Transient
  private BigDecimal dealPrice;
}
