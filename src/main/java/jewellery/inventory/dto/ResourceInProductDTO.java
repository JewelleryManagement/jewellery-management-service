package jewellery.inventory.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ResourceInProductDTO {
    private UUID id;
    private ResourceDTO resource;
    private double quantity;
    private ProductDTO product;
}
