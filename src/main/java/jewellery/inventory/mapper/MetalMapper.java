package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.MetalRequestDto;
import jewellery.inventory.dto.response.resource.MetalResponseDto;
import jewellery.inventory.model.resource.Metal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = StringTrimmer.class)
public interface MetalMapper {
  MetalMapper INSTANCE = Mappers.getMapper(MetalMapper.class);

  MetalResponseDto toResourceResponse(Metal entity);

  Metal toResourceEntity(MetalRequestDto dto);
}
