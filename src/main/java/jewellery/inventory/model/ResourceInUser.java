package jewellery.inventory.model;

import jakarta.persistence.*;

import java.util.UUID;
import jewellery.inventory.model.resource.Resource;
import lombok.*;

@Entity
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceInUser {
  @Id @GeneratedValue private UUID id;

  @ManyToOne private User owner;

  @ManyToOne private Resource resource;

  private double quantity;

  @Transient
  private double dealPrice;
}
