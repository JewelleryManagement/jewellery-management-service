package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.*;
import jewellery.inventory.dto.response.resource.*;
import jewellery.inventory.exception.MappingException;
import jewellery.inventory.model.resource.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceMapper {
  private final PearlMapper pearlMapper;
  private final GemstoneMapper gemstoneMapper;
  private final LinkingPartMapper linkingPartMapper;
  private final PreciousMetalMapper preciousMetalMapper;

  public ResourceResponseDto toResourceResponse(Resource resource) {
    if (resource instanceof Pearl) {
      return pearlMapper.toResourceResponse((Pearl) resource);
    } else if (resource instanceof Gemstone) {
      return gemstoneMapper.toResourceResponse((Gemstone) resource);
    } else if (resource instanceof LinkingPart) {
      return linkingPartMapper.toResourceResponse((LinkingPart) resource);
    } else if (resource instanceof PreciousMetal) {
      return preciousMetalMapper.toResourceResponse((PreciousMetal) resource);
    }
    throw new MappingException(resource);
  }

  public Resource toResourceEntity(ResourceRequestDto resourceRequestDto) {
    if (resourceRequestDto instanceof PearlRequestDto pearlDto) {
      return pearlMapper.toResourceEntity(pearlDto);
    }
    if (resourceRequestDto instanceof GemstoneRequestDto gemstoneDTO) {
      return gemstoneMapper.toResourceEntity(gemstoneDTO);
    }
    if (resourceRequestDto instanceof PreciousMetalRequestDto preciousMetalDto) {
      return preciousMetalMapper.toResourceEntity(preciousMetalDto);
    }
    if (resourceRequestDto instanceof LinkingPartRequestDto linkingPartDTO) {
      return linkingPartMapper.toResourceEntity(linkingPartDTO);
    }
    throw new MappingException(resourceRequestDto);
  }
}
