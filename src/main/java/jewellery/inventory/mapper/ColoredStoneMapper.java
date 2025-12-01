package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.ColoredStoneRequestDto;
import jewellery.inventory.dto.response.resource.ColoredStoneResponseDto;
import jewellery.inventory.model.resource.ColoredStone;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = StringTrimmer.class)
public interface ColoredStoneMapper {
  ColoredStoneMapper INSTANCE = Mappers.getMapper(ColoredStoneMapper.class);

  ColoredStoneResponseDto toResourceResponse(ColoredStone entity);

  ColoredStone toResourceEntity(ColoredStoneRequestDto dto);
}
