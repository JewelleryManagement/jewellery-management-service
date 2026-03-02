package jewellery.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Sale {
  @Id @GeneratedValue private UUID id;
  @ManyToOne private User seller;
  @ManyToOne private User buyer;

  @JoinColumn(name = "organization_seller_id")
  @ManyToOne
  private Organization organizationSeller;

  @OneToMany(mappedBy = "sale", orphanRemoval = true, cascade = CascadeType.ALL)
  private List<ProductPriceDiscount> products;

  @OneToMany(mappedBy = "partOfSale", orphanRemoval = true, cascade = CascadeType.PERSIST)
  private List<PurchasedResourceInUser> resources;

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
  private LocalDateTime date;
}
