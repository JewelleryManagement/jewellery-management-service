package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.resource.ResourceInUser;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
  @Id @GeneratedValue private UUID id;

  @Column(unique = true)
  private String name;

  @Column(unique = true)
  private String email;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<Product> productsOwned = new ArrayList<>();

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ResourceInUser> resourcesOwned = new ArrayList<>();

  public void addResource(ResourceInUser resourceInUser) {
    resourcesOwned.add(resourceInUser);
    resourceInUser.setOwner(this);
  }

}
