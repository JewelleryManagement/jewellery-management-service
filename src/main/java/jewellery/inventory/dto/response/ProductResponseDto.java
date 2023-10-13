package jewellery.inventory.dto.response;

import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private UUID id;
    private List<String> authors;

    private UserResponseDto owner;

    private List<ResourceQuantityResponseDto> resourcesContent;
    private List<ProductResponseDto> productsContent;

    private UUID contentOf;
    private String description;
    private double salePrice;
    private boolean isSold;
}
