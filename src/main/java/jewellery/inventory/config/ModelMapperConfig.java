package jewellery.inventory.config;

import jewellery.inventory.dto.*;
import jewellery.inventory.model.resources.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper resourceMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(Resource.class, ResourceDTO.class)
                .include(Gemstone.class, GemstoneDTO.class)
                .include(LinkingPart.class, LinkingPartDTO.class)
                .include(Pearl.class, PearlDTO.class)
                .include(PreciousMetal.class, PreciousMetalDTO.class);

        modelMapper.createTypeMap(ResourceDTO.class, Resource.class)
                .include(GemstoneDTO.class, Gemstone.class)
                .include(LinkingPartDTO.class, LinkingPart.class)
                .include(PearlDTO.class, Pearl.class)
                .include(PreciousMetalDTO.class, PreciousMetal.class);

        return modelMapper;
    }
}
