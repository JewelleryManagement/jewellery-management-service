package jewellery.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
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
