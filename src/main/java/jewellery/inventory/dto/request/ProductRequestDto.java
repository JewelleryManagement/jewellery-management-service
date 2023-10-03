package jewellery.inventory.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    private String name;
    private List<String> authors;
    private String ownerName;

    List<ResourceInProductRequestDto> resourcesContent;
    private List<UUID> productsContent;

    private String description;
    private double salePrice;

}
