package jewellery.inventory.dto;

import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import lombok.Data;

@Data
public class ResourceQuantityDto {
    private ResourceResponseDto resource;
    private double quantity;
}
