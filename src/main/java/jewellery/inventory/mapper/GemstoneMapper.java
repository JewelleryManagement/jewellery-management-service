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
        return String.format("%.1fx%.1fx%.1f", gemstone.getDimensionX(), gemstone.getDimensionY(), gemstone.getDimensionZ());
    }

    default void setDimensions(GemstoneRequestDto dto) {
        String size = dto.getSize();
        if (size == null) {
            throw new IllegalArgumentException("Size cannot be null");
        }

        String[] dimensions = size.split("x");
        if (dimensions.length != 3) {
            throw new IllegalArgumentException("Invalid size format. Must be in the format '1.00x2.00x3.00'");
        }
        try {
            double dimensionX = Double.parseDouble(dimensions[0].trim());
            double dimensionY = Double.parseDouble(dimensions[1].trim());
            double dimensionZ = Double.parseDouble(dimensions[2].trim());

            dto.setDimensionX(Double.parseDouble(String.valueOf(dimensionX)));
            dto.setDimensionY(Double.parseDouble(String.valueOf(dimensionY)));
            dto.setDimensionZ(Double.parseDouble(String.valueOf(dimensionZ)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid size format. Must be in the format '1.00x2.00x3.00'");
        }

    }
}
