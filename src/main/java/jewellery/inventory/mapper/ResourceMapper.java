package jewellery.inventory.mapper;

import jewellery.inventory.dto.*;
import jewellery.inventory.model.resources.*;

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
        return null;
    }

    public static Pearl map(PearlDTO pearlDto) {
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

    public static PreciousMetal map(PreciousMetalDTO preciousMetalDto) {
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

    public static Gemstone map(GemstoneDTO gemstoneDTO) {
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

    public static LinkingPart map(LinkingPartDTO linkingPartDTO) {
        return LinkingPart.builder()
                .id(null)
                .clazz(linkingPartDTO.getClazz())
                .quantityType(linkingPartDTO.getQuantityType())
                .description(linkingPartDTO.getDescription())
                .build();
    }


    //TODO: Implement this
    public static ResourceDTO map(Resource resource) {
        return ResourceDTO.builder().build();
    }
}
