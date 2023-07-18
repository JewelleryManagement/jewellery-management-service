package jewellery.inventory.model.resources;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Resource {

  @Id @GeneratedValue private UUID id;

  private String name;
  private String quantityType;

  @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
  private List<ResourceInUser> userAffiliations;

  @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
  private List<ResourceInProduct> productAffiliations;
}
