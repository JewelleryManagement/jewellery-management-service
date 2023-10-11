package jewellery.inventory.dto.request;

import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
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
    private UUID ownerId;
    List<ResourceQuantityRequestDto> resourcesContent;
    private List<UUID> productsContent;
    private String description;
    private double salePrice;

}
