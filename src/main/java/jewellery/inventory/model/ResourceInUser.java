package jewellery.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

  @JsonIgnore
  @ManyToOne private User owner;

  @JsonIgnore
  @ManyToOne private Resource resource;

  private double quantity;
}
