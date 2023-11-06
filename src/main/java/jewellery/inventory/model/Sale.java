package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.Date;
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

  private Date date;
}
