package jewellery.inventory.model.resources;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LinkingPart extends Resource {
  private String description;
}
