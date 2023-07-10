package jewellery.inventory.DTO;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.resources.ResourceInUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private UUID id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Product> productsOwned;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ResourceInUser> resourcesOwned;
}
