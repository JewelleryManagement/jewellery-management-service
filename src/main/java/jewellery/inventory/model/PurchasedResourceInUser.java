package jewellery.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class PurchasedResourceInUser {
  @Id @GeneratedValue private UUID id;

  @ManyToOne private Resource resource;

  private BigDecimal quantity;

  private BigDecimal salePrice;

  private BigDecimal discount;

  @ManyToOne private String partOfSale;
}
