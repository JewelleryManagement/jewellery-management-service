package jewellery.inventory.model;

import jewellery.inventory.model.resources.ResourceInUser;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Product> productsOwned;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ResourceInUser> resourcesOwned;
}
