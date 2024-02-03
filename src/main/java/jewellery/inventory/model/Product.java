package jewellery.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.resource.ResourceInProduct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Product {
  @Id @GeneratedValue private UUID id;

  @ManyToMany
  @JoinTable(
      name = "product_author",
      joinColumns = @JoinColumn(name = "product_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> authors;

  @ManyToOne private User owner;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private List<ResourceInProduct> resourcesContent;

  @ManyToOne
  @JoinColumn(name = "content_of")
  private Product contentOf;

  @OneToMany(mappedBy = "contentOf", cascade = CascadeType.ALL)
  private List<Product> productsContent;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "image_id", referencedColumnName = "id")
  private Image image;

  private String catalogNumber;
  private String productionNumber;
  private String description;
  private BigDecimal salePrice;
  private BigDecimal discount;
  @ManyToOne private Sale partOfSale;
  @ManyToOne private Organization organization;
}
