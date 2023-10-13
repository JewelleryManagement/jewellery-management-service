package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    private List<String> authors;
    private UUID ownerId;
    @NotNull
    List<ResourceQuantityRequestDto> resourcesContent;
    private List<UUID> productsContent;
    private String description;
    private double salePrice;

}
