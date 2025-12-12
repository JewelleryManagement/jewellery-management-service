package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.ColoredStoneMeleeRequestDto;
import jewellery.inventory.dto.response.resource.ColoredStoneMeleeResponseDto;
import jewellery.inventory.model.resource.ColoredStoneMelee;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = StringTrimmer.class)
public interface ColoredStoneMeleeMapper {
  ColoredStoneMeleeMapper INSTANCE = Mappers.getMapper(ColoredStoneMeleeMapper.class);

  ColoredStoneMeleeResponseDto toResourceResponse(ColoredStoneMelee entity);

  ColoredStoneMelee toResourceEntity(ColoredStoneMeleeRequestDto dto);
}
