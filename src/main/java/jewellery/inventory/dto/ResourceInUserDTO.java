package jewellery.inventory.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ResourceInUserDTO {
    private UUID id;
    private UserDTO owner;
    private ResourceDTO resource;
    private double quantity;
}
