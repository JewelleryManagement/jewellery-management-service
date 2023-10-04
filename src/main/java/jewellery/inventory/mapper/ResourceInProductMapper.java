package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.resource.ResourceInProductResponseDto;
import jewellery.inventory.model.resource.ResourceInProduct;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceInProductMapper {

    ResourceInProductResponseDto toResourceInProductResponseDto(ResourceInProduct resourceInProduct);
}
