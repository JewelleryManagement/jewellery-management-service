package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ImageResponseDto;
import jewellery.inventory.model.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageDataMapper {

  public ImageResponseDto toImageResponse(Image image) {
    return ImageResponseDto.builder()
        .name(image.getName())
        .type(image.getType())
        .filePath(image.getFilePath())
        .productId(image.getProduct().getId())
        .build();
  }
}
