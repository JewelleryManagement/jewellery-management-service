package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.DiamondMeleeRequestDto;
import jewellery.inventory.dto.response.resource.DiamondMeleeResponseDto;
import jewellery.inventory.model.resource.DiamondMelee;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = StringTrimmer.class)
public interface DiamondMeleeMapper {
  DiamondMeleeMapper INSTANCE = Mappers.getMapper(DiamondMeleeMapper.class);

  DiamondMeleeResponseDto toResourceResponse(DiamondMelee entity);

  DiamondMelee toResourceEntity(DiamondMeleeRequestDto dto);
}
