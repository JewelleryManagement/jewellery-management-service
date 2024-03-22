package jewellery.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "product_price_discount")
public class ProductPriceDiscount {
  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne
  @JoinColumn(name = "sale_id")
  private Sale sale;

  private BigDecimal salePrice;
  private BigDecimal discount;
}
