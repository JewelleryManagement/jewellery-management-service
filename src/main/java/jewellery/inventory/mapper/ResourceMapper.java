package jewellery.inventory.mapper;

import java.math.BigDecimal;
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
  private final ColoredStoneMapper coloredStoneMapper;
  private final ColoredStoneMeleeMapper coloredStoneMeleeMapper;
  private final ElementMapper elementMapper;
  private final MetalMapper metalMapper;
  private final DiamondMeleeMapper diamondMeleeMapper;
  private final SemiPreciousStoneMapper semiPreciousStoneMapper;

  public ResourceResponseDto toResourceResponse(Resource resource) {
    if (resource == null) {
      return null;
    }
    if (resource instanceof Pearl pearl) {
      return pearlMapper.toResourceResponse(pearl);
    } else if (resource instanceof Diamond diamond) {
      return toDiamondResponseWithSize(diamond, diamondMapper.toResourceResponse(diamond));
    } else if (resource instanceof ColoredStone coloredStone) {
      return toColoredStoneResponseWithSize(
          coloredStone, coloredStoneMapper.toResourceResponse(coloredStone));
    } else if (resource instanceof ColoredStoneMelee coloredStoneMelee) {
      return coloredStoneMeleeMapper.toResourceResponse(coloredStoneMelee);
    } else if (resource instanceof Element element) {
      return elementMapper.toResourceResponse(element);
    } else if (resource instanceof Metal metal) {
      return metalMapper.toResourceResponse(metal);
    } else if (resource instanceof DiamondMelee diamondMelee) {
      return (diamondMeleeMapper.toResourceResponse(diamondMelee));
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
    if (resourceRequestDto instanceof ColoredStoneRequestDto coloredStoneRequestDto) {
      return coloredStoneMapper.toResourceEntity(coloredStoneRequestDto);
    }
    if (resourceRequestDto instanceof ColoredStoneMeleeRequestDto coloredStoneMeleeRequestDto) {
      return coloredStoneMeleeMapper.toResourceEntity(coloredStoneMeleeRequestDto);
    }
    if (resourceRequestDto instanceof MetalRequestDto metalDto) {
      return metalMapper.toResourceEntity(metalDto);
    }
    if (resourceRequestDto instanceof ElementRequestDto linkingPartDTO) {
      return elementMapper.toResourceEntity(linkingPartDTO);
    }
    if (resourceRequestDto instanceof DiamondMeleeRequestDto diamondMeleeRequestDto) {
      return diamondMeleeMapper.toResourceEntity(diamondMeleeRequestDto);
    }
    if (resourceRequestDto instanceof SemiPreciousStoneRequestDto semiPreciousStoneRequestDto) {
      return semiPreciousStoneMapper.toResourceEntity(semiPreciousStoneRequestDto);
    }
    throw new MappingException(resourceRequestDto);
  }

  private ResourceResponseDto toDiamondResponseWithSize(
      Diamond diamond, DiamondResponseDto diamondResponseDto) {
    String size =
        formatSize(diamond.getDimensionX(), diamond.getDimensionY(), diamond.getDimensionZ());

    diamondResponseDto.setSize(size);
    return diamondResponseDto;
  }

  private ResourceResponseDto toColoredStoneResponseWithSize(
      ColoredStone coloredStone, ColoredStoneResponseDto coloredStoneResponseDto) {
    String size =
        formatSize(
            coloredStone.getDimensionX(),
            coloredStone.getDimensionY(),
            coloredStone.getDimensionZ());

    coloredStoneResponseDto.setSize(size);
    return coloredStoneResponseDto;
  }

  private String formatSize(BigDecimal x, BigDecimal y, BigDecimal z) {
    return String.format(Locale.US, "%.2fx%.2fx%.2f", x, y, z);
  }
}
