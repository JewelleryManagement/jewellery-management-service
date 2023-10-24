package jewellery.inventory.mapper;

import jewellery.inventory.dto.EntityLogDto;
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
  private final EntityLogMapper entityLogMapper;

  public ResourceResponseDto toResourceResponse(Resource resource) {
    if (resource instanceof Pearl pearl) {
      return pearlMapper.toResourceResponse(pearl);
    } else if (resource instanceof Gemstone gemstone) {
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

  public EntityLogDto toEntityLogDto(Resource resource) {
    if (resource instanceof LinkingPart linkingPart) {
      LinkingPartResponseDto linkingPartDto = linkingPartMapper.toResourceResponse(linkingPart);
      return entityLogMapper.toEntityLogDto(linkingPartDto);
    } else if (resource instanceof Pearl pearl) {
      PearlResponseDto pearlDto = pearlMapper.toResourceResponse(pearl);
      return entityLogMapper.toEntityLogDto(pearlDto);
    } else if (resource instanceof Gemstone gemstone) {
      GemstoneResponseDto gemstoneDto = gemstoneMapper.toResourceResponse(gemstone);
      return entityLogMapper.toEntityLogDto(gemstoneDto);
    } else if (resource instanceof PreciousMetal preciousMetal) {
      PreciousMetalResponseDto preciousMetalDto =
          preciousMetalMapper.toResourceResponse(preciousMetal);
      return entityLogMapper.toEntityLogDto(preciousMetalDto);
    }

    throw new MappingException(resource);
  }
}
