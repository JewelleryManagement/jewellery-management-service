package jewellery.inventory.model;

import jewellery.inventory.model.resource.ResourceInUser;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Product> productsOwned;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<ResourceInUser> resourcesOwned;
}
