package jewellery.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.resources.ResourceInUser;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

  @Id @GeneratedValue private UUID id;

  @NotEmpty
  @Size(min = 3, max = 64, message = "Size must be between 3 and 64")
  @Pattern(
      regexp = "^(?!.*__)[A-Za-z0-9_]*$",
      message =
          "Name must only contain alphanumeric characters and underscores, and no consecutive underscores")
  @Column(unique = true)
  private String name;

  @NotEmpty
  @Pattern(
      regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
      message = "Email must be valid")
  @Column(unique = true)
  private String email;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<Product> productsOwned;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<ResourceInUser> resourcesOwned;
}
