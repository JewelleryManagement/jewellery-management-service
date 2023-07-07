package jewellery.inventory.model;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL.ALL)
//    private List<Product> productsOwned;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<ResourceInUser> resourcesOwned;


}
