package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.ElementRequestDto;
import jewellery.inventory.dto.response.resource.ElementResponseDto;
import jewellery.inventory.model.resource.Element;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ElementMapper {
  ElementMapper INSTANCE = Mappers.getMapper(ElementMapper.class);

  ElementResponseDto toResourceResponse(Element entity);

  Element toResourceEntity(ElementRequestDto dto);
}
