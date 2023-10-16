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
}
