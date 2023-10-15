package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.GemstoneRequestDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.model.resource.Gemstone;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GemstoneMapper {
  GemstoneMapper INSTANCE = Mappers.getMapper(GemstoneMapper.class);

  GemstoneResponseDto toResourceResponse(Gemstone entity);

    Gemstone toResourceEntity(GemstoneRequestDto dto);


    default String getGemstoneSizeString(Gemstone gemstone) {
        return String.format("%.0fx%.0fx%.0f", gemstone.getDimensionX(), gemstone.getDimensionY(), gemstone.getDimensionZ());
    }

    default void setDimensions(GemstoneRequestDto dto) {
        String[] dimensions = dto.getSize().split("x");
        if (dimensions.length == 3) {
            dto.setDimensionX(Double.parseDouble(dimensions[0].trim()));
            dto.setDimensionY(Double.parseDouble(dimensions[1].trim()));
            dto.setDimensionZ(Double.parseDouble(dimensions[2].trim()));
        }
    }
}
