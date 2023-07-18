package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.resources.ResourceInProduct;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {
  @Id @GeneratedValue private UUID id;

  @ElementCollection private List<String> authors;

  @ManyToOne private User owner;

  @Lob private byte[] picture;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private List<ResourceInProduct> resourcesContent;

  @ManyToMany private List<Product> productsContent;

  private String description;
  private double salePrice;
  private boolean isSold;
}
