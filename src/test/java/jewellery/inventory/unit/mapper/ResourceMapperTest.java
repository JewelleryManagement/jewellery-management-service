package jewellery.inventory.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exeption.MappingException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ResourceMapperTest {

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndResponseDtos")
  void willMapValidResourceToDTO(
      Resource resource, ResourceResponseDto expectedResourceResponseDto) {
    assertEquals(expectedResourceResponseDto, ResourceMapper.map(resource));
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndRequestDtos")
  void willMapValidDtoToResource(Resource expectedResource, ResourceRequestDto resourceRequestDto) {
    expectedResource.setId(null);
    assertEquals(expectedResource, ResourceMapper.map(resourceRequestDto));
  }

  @Test
  void willFailToMapInvalidResourceToDTO() {
    Resource invalidResource = new Resource();
    assertThrows(MappingException.class, () -> ResourceMapper.map(invalidResource));
  }

  @Test
  void willFailToMapInvalidDtoToResource() {
    ResourceRequestDto invalidResourceRequestDto = new ResourceRequestDto();
    assertThrows(MappingException.class, () -> ResourceMapper.map(invalidResourceRequestDto));
  }
}
