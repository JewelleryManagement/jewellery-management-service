package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.resource.*;
import jewellery.inventory.dto.response.resource.*;
import jewellery.inventory.exeption.MappingException;
import jewellery.inventory.model.resource.*;

public class ResourceMapper {
  private ResourceMapper() {}

  public static Resource map(ResourceRequestDto resourceRequestDto) {
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

    throw new MappingException(String.format("Can't map resourceDTO: %s", resourceRequestDto));
  }

  public static ResourceResponseDto map(Resource resource) {
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
    throw new MappingException(String.format("Can't map resource: %s", resource));
  }

  private static Pearl map(PearlResponseDto pearlDto) {
    return Pearl.builder()
        .id(null)
        .clazz(pearlDto.getClazz())
        .quantityType(pearlDto.getQuantityType())
        .type(pearlDto.getType())
        .size(pearlDto.getSize())
        .quality(pearlDto.getQuality())
        .color(pearlDto.getColor())
        .shape(pearlDto.getShape())
        .build();
  }

  private static PreciousMetal map(PreciousMetalResponseDto preciousMetalDto) {
    return PreciousMetal.builder()
        .id(null)
        .clazz(preciousMetalDto.getClazz())
        .quantityType(preciousMetalDto.getQuantityType())
        .type(preciousMetalDto.getType())
        .plating(preciousMetalDto.getPlating())
        .color(preciousMetalDto.getColor())
        .type(preciousMetalDto.getType())
        .purity(preciousMetalDto.getPurity())
        .build();
  }

  private static Gemstone map(GemstoneResponseDto gemstoneDTO) {
    return Gemstone.builder()
        .id(null)
        .clazz(gemstoneDTO.getClazz())
        .quantityType(gemstoneDTO.getQuantityType())
        .carat(gemstoneDTO.getCarat())
        .color(gemstoneDTO.getColor())
        .cut(gemstoneDTO.getCut())
        .clarity(gemstoneDTO.getClarity())
        .dimensionX(gemstoneDTO.getDimensionX())
        .dimensionY(gemstoneDTO.getDimensionY())
        .dimensionZ(gemstoneDTO.getDimensionZ())
        .shape(gemstoneDTO.getShape())
        .build();
  }

  private static LinkingPart map(LinkingPartResponseDto linkingPartDTO) {
    return LinkingPart.builder()
        .id(null)
        .clazz(linkingPartDTO.getClazz())
        .quantityType(linkingPartDTO.getQuantityType())
        .description(linkingPartDTO.getDescription())
        .build();
  }

  private static PearlResponseDto map(Pearl pearl) {
    return PearlResponseDto.builder()
        .id(pearl.getId())
        .clazz(pearl.getClazz())
        .quantityType(pearl.getQuantityType())
        .type(pearl.getType())
        .size(pearl.getSize())
        .quality(pearl.getQuality())
        .color(pearl.getColor())
        .shape(pearl.getShape())
        .build();
  }

  private static PreciousMetalResponseDto map(PreciousMetal preciousMetal) {
    return PreciousMetalResponseDto.builder()
        .id(preciousMetal.getId())
        .clazz(preciousMetal.getClazz())
        .quantityType(preciousMetal.getQuantityType())
        .type(preciousMetal.getType())
        .plating(preciousMetal.getPlating())
        .color(preciousMetal.getColor())
        .type(preciousMetal.getType())
        .purity(preciousMetal.getPurity())
        .build();
  }

  private static GemstoneResponseDto map(Gemstone gemstone) {
    return GemstoneResponseDto.builder()
        .id(gemstone.getId())
        .clazz(gemstone.getClazz())
        .quantityType(gemstone.getQuantityType())
        .carat(gemstone.getCarat())
        .color(gemstone.getColor())
        .cut(gemstone.getCut())
        .clarity(gemstone.getClarity())
        .dimensionX(gemstone.getDimensionX())
        .dimensionY(gemstone.getDimensionY())
        .dimensionZ(gemstone.getDimensionZ())
        .shape(gemstone.getShape())
        .build();
  }

  private static LinkingPartResponseDto map(LinkingPart linkingPart) {
    return LinkingPartResponseDto.builder()
        .id(linkingPart.getId())
        .clazz(linkingPart.getClazz())
        .quantityType(linkingPart.getQuantityType())
        .description(linkingPart.getDescription())
        .build();
  }
}
