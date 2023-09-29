package jewellery.inventory.dto.response;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private String name;
    private List<String> authors;
    private User owner;
//    private byte[] picture;
    private List<Resource> resourcesContent;
    private List<Product> productsContent;

    private String description;
    private double salePrice;
    private boolean isSold;
}
