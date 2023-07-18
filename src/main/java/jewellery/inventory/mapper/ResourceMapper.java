package jewellery.inventory.mapper;

import jewellery.inventory.dto.*;
import jewellery.inventory.exeption.MappingException;
import jewellery.inventory.model.resource.*;

public class ResourceMapper {
    private ResourceMapper() {
    }

    public static Resource map(ResourceDTO resourceDTO) {
        if (resourceDTO instanceof PearlDTO pearlDTO) {
            return map(pearlDTO);
        }
        if (resourceDTO instanceof GemstoneDTO gemstoneDTO) {
            return map(gemstoneDTO);
        }
        if(resourceDTO instanceof PreciousMetalDTO preciousMetalDto){
            return map(preciousMetalDto);
        }
        if(resourceDTO instanceof LinkingPartDTO linkingPartDTO){
            return map(linkingPartDTO);
        }

        throw new MappingException(String.format("Can't map resourceDTO: %s", resourceDTO));
    }

    private static Pearl map(PearlDTO pearlDto) {
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

    private static PreciousMetal map(PreciousMetalDTO preciousMetalDto) {
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

    private static Gemstone map(GemstoneDTO gemstoneDTO) {
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

    private static LinkingPart map(LinkingPartDTO linkingPartDTO) {
        return LinkingPart.builder()
                .id(null)
                .clazz(linkingPartDTO.getClazz())
                .quantityType(linkingPartDTO.getQuantityType())
                .description(linkingPartDTO.getDescription())
                .build();
    }


    public static ResourceDTO map(Resource resource) {
        if (resource instanceof Pearl pearl) {
            return map(pearl);
        }
        if (resource instanceof Gemstone gemstone) {
            return map(gemstone);
        }
        if(resource instanceof PreciousMetal preciousMetal){
            return map(preciousMetal);
        }
        if(resource instanceof LinkingPart linkingPart){
            return map(linkingPart);
        }
        throw new MappingException(String.format("Can't map resource: %s", resource));
    }

    private static PearlDTO map(Pearl pearl) {
        return PearlDTO.builder()
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

    private static PreciousMetalDTO map(PreciousMetal preciousMetal) {
        return PreciousMetalDTO.builder()
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

    private static GemstoneDTO map(Gemstone gemstone) {
        return GemstoneDTO.builder()
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

    private static LinkingPartDTO map(LinkingPart linkingPart) {
        return LinkingPartDTO.builder()
                .id(linkingPart.getId())
                .clazz(linkingPart.getClazz())
                .quantityType(linkingPart.getQuantityType())
                .description(linkingPart.getDescription())
                .build();
    }
}
