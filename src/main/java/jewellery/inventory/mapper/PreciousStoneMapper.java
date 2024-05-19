package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.PreciousStoneRequestDto;
import jewellery.inventory.dto.response.resource.PreciousStoneResponseDto;
import jewellery.inventory.model.resource.PreciousStone;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = StringTrimmer.class)
public interface PreciousStoneMapper {
  PreciousStoneMapper INSTANCE = Mappers.getMapper(PreciousStoneMapper.class);

  PreciousStoneResponseDto toResourceResponse(PreciousStone entity);

  PreciousStone toResourceEntity(PreciousStoneRequestDto dto);
}
