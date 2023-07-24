package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.PreciousMetalRequestDto;
import jewellery.inventory.dto.response.resource.PreciousMetalResponseDto;
import jewellery.inventory.model.resource.PreciousMetal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PreciousMetalMapper {
  PreciousMetalMapper INSTANCE = Mappers.getMapper(PreciousMetalMapper.class);

  PreciousMetalResponseDto toResourceResponse(PreciousMetal entity);

  PreciousMetal toResourceEntity(PreciousMetalRequestDto dto);
}
