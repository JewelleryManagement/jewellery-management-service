package jewellery.inventory.unit.mapper;

import static jewellery.inventory.mapper.ResourceMapper.toResourceEntity;
import static jewellery.inventory.mapper.ResourceMapper.toResourceResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.MappingException;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ResourceMapperTest {

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndResponseDtos")
  void willMapValidResourceToDTO(
      Resource resource, ResourceResponseDto expectedResourceResponseDto) {
    assertEquals(expectedResourceResponseDto, toResourceResponse(resource));
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndRequestDtos")
  void willMapValidDtoToResource(Resource expectedResource, ResourceRequestDto resourceRequestDto) {
    expectedResource.setId(null);
    assertEquals(expectedResource, toResourceEntity(resourceRequestDto));
  }

  @Test
  void willFailToMapInvalidResourceToDTO() {
    Resource invalidResource = new Resource();
    assertThrows(MappingException.class, () -> toResourceResponse(invalidResource));
  }

  @Test
  void willFailToMapInvalidDtoToResource() {
    ResourceRequestDto invalidResourceRequestDto = new ResourceRequestDto();
    assertThrows(MappingException.class, () -> toResourceEntity(invalidResourceRequestDto));
  }
}
