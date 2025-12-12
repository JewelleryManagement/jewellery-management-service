package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.DiamondRequestDto;
import jewellery.inventory.dto.response.resource.DiamondResponseDto;
import jewellery.inventory.model.resource.Diamond;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = StringTrimmer.class)
public interface DiamondMapper {
  DiamondMapper INSTANCE = Mappers.getMapper(DiamondMapper.class);

  DiamondResponseDto toResourceResponse(Diamond entity);

  Diamond toResourceEntity(DiamondRequestDto dto);
}
