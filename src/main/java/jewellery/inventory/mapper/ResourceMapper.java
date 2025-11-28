package jewellery.inventory.mapper;

import java.util.Locale;
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
  private final DiamondMapper diamondMapper;
  private final ElementMapper elementMapper;
  private final MetalMapper metalMapper;
  private final SemiPreciousStoneMapper semiPreciousStoneMapper;

  public ResourceResponseDto toResourceResponse(Resource resource) {
    if (resource == null) {
      return null;
    }
    if (resource instanceof Pearl pearl) {
      return pearlMapper.toResourceResponse(pearl);
    } else if (resource instanceof Diamond diamond) {
      return toDiamondResponseWithSize(diamond, diamondMapper.toResourceResponse(diamond));
    } else if (resource instanceof Element element) {
      return elementMapper.toResourceResponse(element);
    } else if (resource instanceof Metal metal) {
      return metalMapper.toResourceResponse(metal);
    } else if (resource instanceof SemiPreciousStone semiPreciousStone) {
      return (semiPreciousStoneMapper.toResourceResponse(semiPreciousStone));
    }
    throw new MappingException(resource);
  }

  public Resource toResourceEntity(ResourceRequestDto resourceRequestDto) {
    if (resourceRequestDto instanceof PearlRequestDto pearlDto) {
      return pearlMapper.toResourceEntity(pearlDto);
    }
    if (resourceRequestDto instanceof DiamondRequestDto DiamondDTO) {
      return diamondMapper.toResourceEntity(DiamondDTO);
    }
    if (resourceRequestDto instanceof MetalRequestDto metalDto) {
      return metalMapper.toResourceEntity(metalDto);
    }
    if (resourceRequestDto instanceof ElementRequestDto linkingPartDTO) {
      return elementMapper.toResourceEntity(linkingPartDTO);
    }
    if (resourceRequestDto instanceof SemiPreciousStoneRequestDto semiPreciousStoneRequestDto) {
      return semiPreciousStoneMapper.toResourceEntity(semiPreciousStoneRequestDto);
    }
    throw new MappingException(resourceRequestDto);
  }

  private ResourceResponseDto toDiamondResponseWithSize(
      Diamond diamond, DiamondResponseDto diamondResponseDto) {
    String size =
        String.format(
            Locale.US,
            "%.2fx%.2fx%.2f",
            diamond.getDimensionX(),
            diamond.getDimensionY(),
            diamond.getDimensionZ());
    diamondResponseDto.setSize(size);
    return diamondResponseDto;
  }
}
