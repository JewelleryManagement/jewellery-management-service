package jewellery.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.model.resource.Resource;
import lombok.*;

@Entity
@Table(name = "purchased_resources")
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
  @ManyToOne private User owner;
  private BigDecimal quantity;
  private BigDecimal salePrice;
  private BigDecimal discount;
  @ManyToOne
  @JoinColumn(name = "partOfSale_id")
  private Sale partOfSale;
}
