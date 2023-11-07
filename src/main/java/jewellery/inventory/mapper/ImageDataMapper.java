package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.model.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageDataMapper {
  ImageResponseDto toImageResponse(Image image);
}
