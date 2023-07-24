package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.PearlRequestDto;
import jewellery.inventory.dto.response.resource.PearlResponseDto;
import jewellery.inventory.model.resource.Pearl;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PearlMapper {
  PearlMapper INSTANCE = Mappers.getMapper(PearlMapper.class);

  PearlResponseDto toResourceResponse(Pearl entity);

  Pearl toResourceEntity(PearlRequestDto dto);
  //    PearlRequestDto toResourceRequest(Pearl entity);
  //    Pearl toResourceEntity(PearlResponseDto dto);
}
