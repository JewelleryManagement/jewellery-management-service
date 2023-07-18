package jewellery.inventory.model.resources;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import jewellery.inventory.model.User;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ResourceInUser {
  @Id @GeneratedValue private UUID id;

  @ManyToOne private User owner;

  @ManyToOne private Resource resource;

  private double quantity;
}
