package jewellery.inventory.dto;

import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceQuantityDto {
    private ResourceResponseDto resource;
    private double quantity;
}
