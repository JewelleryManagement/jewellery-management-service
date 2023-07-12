package jewellery.inventory.dto;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.resources.ResourceInUser;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private List<ProductDTO> productsOwned;
    private List<ResourceInUserDTO> resourcesOwned;
}
