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
    if (resource instanceof Pearl pearl) {
      return pearlMapper.toResourceResponse(pearl);
    } else if (resource instanceof Gemstone gemstone) {
      gemstone.setSize(gemstoneMapper.getGemstoneSizeString(gemstone));
      return gemstoneMapper.toResourceResponse(gemstone);
    } else if (resource instanceof LinkingPart linkingPart) {
      return linkingPartMapper.toResourceResponse(linkingPart);
    } else if (resource instanceof PreciousMetal preciousMetal) {
      return preciousMetalMapper.toResourceResponse(preciousMetal);
    }
    throw new MappingException(resource);
  }

  public Resource toResourceEntity(ResourceRequestDto resourceRequestDto) {
    if (resourceRequestDto instanceof PearlRequestDto pearlDto) {
      return pearlMapper.toResourceEntity(pearlDto);
    }
    if (resourceRequestDto instanceof GemstoneRequestDto gemstoneDTO) {
     gemstoneMapper.setDimensions(gemstoneDTO);
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
