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
  private final PreciousStoneMapper preciousStoneMapper;
  private final ElementMapper elementMapper;
  private final MetalMapper metalMapper;
  private final SemiPreciousStoneMapper semiPreciousStoneMapper;

  public ResourceResponseDto toResourceResponse(Resource resource) {
    if (resource instanceof Pearl pearl) {
      return pearlMapper.toResourceResponse(pearl);
    } else if (resource instanceof PreciousStone preciousStone) {
      return toPreciousStoneResponseWithSize (preciousStone, preciousStoneMapper.toResourceResponse(preciousStone));
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
    if (resourceRequestDto instanceof PreciousStoneRequestDto preciousStoneDTO) {
      return preciousStoneMapper.toResourceEntity(preciousStoneDTO);
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

  private ResourceResponseDto toPreciousStoneResponseWithSize(
      PreciousStone preciousStone, PreciousStoneResponseDto preciousStoneResponseDto) {
    String size =
        String.format(
            Locale.US,
            "%.2fx%.2fx%.2f",
                preciousStone.getDimensionX(),
                preciousStone.getDimensionY(),
                preciousStone.getDimensionZ());
    preciousStoneResponseDto.setSize(size);
    return preciousStoneResponseDto;
  }
}
