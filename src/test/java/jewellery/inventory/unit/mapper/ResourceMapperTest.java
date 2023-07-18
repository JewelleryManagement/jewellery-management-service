package jewellery.inventory.unit.mapper;

import jewellery.inventory.dto.ResourceDTO;
import jewellery.inventory.exeption.MappingException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceMapperTest {

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndDtos")
  void willMapValidResourceToDTO(Resource resource, ResourceDTO expectedResourceDTO) {
    assertEquals(expectedResourceDTO, ResourceMapper.map(resource));
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndDtos")
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
}
