package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.LinkingPartRequestDto;
import jewellery.inventory.dto.response.resource.LinkingPartResponseDto;
import jewellery.inventory.model.resource.LinkingPart;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LinkingPartMapper {
  LinkingPartMapper INSTANCE = Mappers.getMapper(LinkingPartMapper.class);

  LinkingPartResponseDto toResourceResponse(LinkingPart entity);

  LinkingPart toResourceEntity(LinkingPartRequestDto dto);
  //    PearlRequestDto toResourceRequest(Pearl entity);
  //    Pearl toResourceEntity(PearlResponseDto dto);
}
