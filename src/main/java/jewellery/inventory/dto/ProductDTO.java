package jewellery.inventory.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class ProductDTO {
    private UUID id;
    private List<String> authors;
    private UserDTO owner;
    private byte[] picture;
    private List<ResourceInProductDTO> resourcesContent;
    private List<ProductDTO> productsContent;
    private String description;
    private double salePrice;
    private boolean isSold;
}
