package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.*;
import jewellery.inventory.dto.response.resource.*;
import jewellery.inventory.exception.MappingException;
import jewellery.inventory.model.resource.*;

public class ResourceMapper {
  private ResourceMapper() {}

  public static Resource toResourceEntity(ResourceRequestDto resourceRequestDto) {
    if (resourceRequestDto instanceof PearlRequestDto pearlDto) {
      return PearlMapper.INSTANCE.toResourceEntity(pearlDto);
    }
    if (resourceRequestDto instanceof GemstoneRequestDto gemstoneDTO) {
      return GemstoneMapper.INSTANCE.toResourceEntity(gemstoneDTO);
    }
    if (resourceRequestDto instanceof PreciousMetalRequestDto preciousMetalDto) {
      return PreciousMetalMapper.INSTANCE.toResourceEntity(preciousMetalDto);
    }
    if (resourceRequestDto instanceof LinkingPartRequestDto linkingPartDTO) {
      return LinkingPartMapper.INSTANCE.toResourceEntity(linkingPartDTO);
    }

    throw new MappingException(resourceRequestDto);
  }

  public static ResourceResponseDto toResourceResponse(Resource resource) {
    if (resource instanceof Pearl pearl) {
      return PearlMapper.INSTANCE.toResourceResponse(pearl);
    }
    if (resource instanceof Gemstone gemstone) {
      return GemstoneMapper.INSTANCE.toResourceResponse(gemstone);
    }
    if (resource instanceof PreciousMetal preciousMetal) {
      return PreciousMetalMapper.INSTANCE.toResourceResponse(preciousMetal);
    }
    if (resource instanceof LinkingPart linkingPart) {
      return LinkingPartMapper.INSTANCE.toResourceResponse(linkingPart);
    }
    throw new MappingException(resource);
  }
}
