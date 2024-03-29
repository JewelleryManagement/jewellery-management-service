package jewellery.inventory.model.resource;

import jakarta.persistence.*;

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

  @ManyToOne(cascade = CascadeType.PERSIST) private Product product;
}
