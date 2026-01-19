package jewellery.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.resource.ResourceInProduct;
import lombok.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode
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
  @EqualsAndHashCode.Exclude
  @JoinColumn(name = "image_id", referencedColumnName = "id")
  private Image image;

  private String catalogNumber;
  private String productionNumber;
  private String description;
  private BigDecimal additionalPrice;

  @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
  private ProductPriceDiscount partOfSale;

  @ManyToOne private Organization organization;

  public ProductPriceDiscount getPartOfSale() {
    Product parentProduct = this.contentOf;

    while (parentProduct != null) {
      if (parentProduct.getPartOfSale() != null) {
        return parentProduct.getPartOfSale();
      }
      parentProduct = parentProduct.getContentOf();
    }
    return this.partOfSale;
  }
}
