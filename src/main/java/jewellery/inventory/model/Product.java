package jewellery.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.resource.ResourceInProduct;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {
  @Id @GeneratedValue private UUID id;

  @Column(name = "name")
  private String name;

  @ElementCollection
  private List<String> authors;

  @JsonIgnore
  @ManyToOne private User owner;

  @Lob private byte[] picture;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private List<ResourceInProduct> resourcesContent;

  @ManyToMany private List<Product> productsContent;

  private String description;
  private double salePrice;
  private boolean isSold;
}
