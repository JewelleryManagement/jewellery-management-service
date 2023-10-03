package jewellery.inventory.dto.response;

import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.User;
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

    private List<ResourceResponseDto> resourcesContent;
    private List<ProductResponseDto> productsContent;

    private String description;
    private double salePrice;
    private boolean isSold;
}
