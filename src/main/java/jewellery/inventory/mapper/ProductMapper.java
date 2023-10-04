package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponseDto toProductResponse(Product product);
}
