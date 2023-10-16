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
      gemstone.setSize(getGemstoneSizeString(gemstone));
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
     setDimensions(gemstoneDTO);
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

  public String getGemstoneSizeString(Gemstone gemstone) {
    return String.format("%.1fx%.1fx%.1f", gemstone.getDimensionX(), gemstone.getDimensionY(), gemstone.getDimensionZ());
  }
  public void setDimensions(GemstoneRequestDto dto) {
    String size = dto.getSize();
    if (size == null) {
      throw new IllegalArgumentException("Size cannot be null");
    }
    String[] dimensions = size.split("x");
    if (dimensions.length != 3) {
      throw new IllegalArgumentException("Invalid size format. Must be in the format '1.00x2.00x3.00'");
    }
    try {
      dto.setDimensionX(Double.parseDouble(dimensions[0].trim()));
      dto.setDimensionY(Double.parseDouble(dimensions[1].trim()));
      dto.setDimensionZ(Double.parseDouble(dimensions[2].trim()));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid size format. Must be in the format '1.00x2.00x3.00'");
    }
  }
}
