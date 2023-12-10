package jewellery.inventory.dto.response;

import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class ResourcePurchaseResponseDto {
    private UserResponseDto owner;
    private List<ResourceQuantityResponseDto> resourcesAndQuantities;
    private double dealPrice;
}
