package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.model.Image;
import jewellery.inventory.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ImageDataMapper {
  @Mapping(target = "productId", expression = "java(getProductId(image.getProduct()))")
  ImageResponseDto toImageResponse(Image image);

  default UUID getProductId(Product product) {
    return product.getId();
  }
}
