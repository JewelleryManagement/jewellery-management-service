package jewellery.inventory.model.resources;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Resources {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String color;
    private String quantityType;

//    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
//    private List<ResourceInUser> userAffiliations;
//
//    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
//    private List<ResourceInProduct> productAffiliations;

}
