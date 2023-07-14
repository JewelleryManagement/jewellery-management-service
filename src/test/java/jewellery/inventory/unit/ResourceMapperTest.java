package jewellery.inventory.unit;

import jewellery.inventory.dto.ResourceDTO;
import jewellery.inventory.exeptions.MappingException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resources.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static jewellery.inventory.util.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceMapperTest {

    @ParameterizedTest
    @MethodSource("provideResourcesAndDtos")
    void willMapValidResourceToDTO(Resource resource, ResourceDTO expectedResourceDTO) {
        assertEquals(expectedResourceDTO, ResourceMapper.map(resource));
    }

    @ParameterizedTest
    @MethodSource("provideResourcesAndDtos")
    void willMapValidDtoToResource(Resource expectedResource, ResourceDTO resourceDTO) {
        expectedResource.setId(null);
        assertEquals(expectedResource, ResourceMapper.map(resourceDTO));
    }

    @Test
    void willFailToMapInvalidResourceToDTO() {
        Resource invalidResource = new Resource();
        assertThrows(MappingException.class, () -> ResourceMapper.map(invalidResource));
    }

    @Test
    void willFailToMapInvalidDtoToResource() {
        ResourceDTO invalidResourceDTO = new ResourceDTO();
        assertThrows(MappingException.class, () -> ResourceMapper.map(invalidResourceDTO));
    }

    private static Stream<Arguments> provideResourcesAndDtos() {
        return Stream.of(
                Arguments.of(getGemstone(), getGemstoneDTO()),
                Arguments.of(getLinkingPart(), getLinkingPartDTO()),
                Arguments.of(getPearl(), getPearlDTO()),
                Arguments.of(getPreciousMetal(), getPreciousMetalDTO())
        );
    }

}
