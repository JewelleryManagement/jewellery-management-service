package jewellery.inventory.model.resource;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.model.Product;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ResourceInProduct {
  @Id @GeneratedValue private UUID id;

  @ManyToOne private Resource resource;

  private BigDecimal quantity;

  @ManyToOne private Product product;
}
