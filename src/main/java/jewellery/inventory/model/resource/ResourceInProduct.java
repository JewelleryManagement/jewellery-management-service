package jewellery.inventory.model.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import jewellery.inventory.model.Product;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ResourceInProduct {
  @Id @GeneratedValue private UUID id;

  @JsonIgnore
  @ManyToOne
  private Resource resource;

  private double quantity;

  @JsonIgnore
  @ManyToOne
  private Product product;
}
