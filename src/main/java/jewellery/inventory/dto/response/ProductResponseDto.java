package jewellery.inventory.dto.response;

import jewellery.inventory.dto.response.resource.ResourceInProductResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private UUID id;
    private String name;
    private List<String> authors;

    private UserResponseDto owner;

    private List<ResourceInProductResponseDto> resourcesContent;
    private List<ProductResponseDto> productsContent;

    private UUID contentId;
    private String description;
    private double salePrice;
    private boolean isSold;
}
