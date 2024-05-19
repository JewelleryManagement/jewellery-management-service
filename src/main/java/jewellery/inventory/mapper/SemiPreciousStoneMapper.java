package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.SemiPreciousStoneRequestDto;
import jewellery.inventory.dto.response.resource.SemiPreciousStoneResponseDto;
import jewellery.inventory.model.resource.SemiPreciousStone;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = StringTrimmer.class)
public interface SemiPreciousStoneMapper {
  SemiPreciousStoneMapper INSTANCE = Mappers.getMapper(SemiPreciousStoneMapper.class);

  SemiPreciousStoneResponseDto toResourceResponse(SemiPreciousStone entity);

  SemiPreciousStone toResourceEntity(SemiPreciousStoneRequestDto dto);
}
