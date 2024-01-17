package jewellery.inventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
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

  @OneToMany(mappedBy = "partOfSale")
  private List<Product> products;

  @OneToMany(mappedBy = "partOfSales")
  private List<PurchasedResourceInUser> resources;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate date;
}
