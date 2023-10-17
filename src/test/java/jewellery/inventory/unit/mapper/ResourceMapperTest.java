package jewellery.inventory.unit.mapper;

import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.MappingException;
import jewellery.inventory.mapper.*;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceMapperTest {
  private ResourceMapper resourceMapper;

  @BeforeEach
  void setUp() {
  //  Locale.setDefault(Locale.US);
    PearlMapper pearlMapper = PearlMapper.INSTANCE;
    GemstoneMapper gemstoneMapper = GemstoneMapper.INSTANCE;
    LinkingPartMapper linkingPartMapper = LinkingPartMapper.INSTANCE;
    PreciousMetalMapper preciousMetalMapper = PreciousMetalMapper.INSTANCE;
    resourceMapper =
        new ResourceMapper(pearlMapper, gemstoneMapper, linkingPartMapper, preciousMetalMapper);
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.helper.ResourceTestHelper#provideResourcesAndResponseDtos")
  void willMapValidResourceToDTO(
      Resource resource, ResourceResponseDto expectedResourceResponseDto) {
    assertEquals(expectedResourceResponseDto, resourceMapper.toResourceResponse(resource));
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.helper.ResourceTestHelper#provideResourcesAndRequestDtos")
  void willMapValidDtoToResource(Resource expectedResource, ResourceRequestDto resourceRequestDto) {
    expectedResource.setId(null);
    assertEquals(expectedResource, resourceMapper.toResourceEntity(resourceRequestDto));
  }

  @Test
  void willFailToMapInvalidResourceToDTO() {
    Resource invalidResource = new Resource();
    assertThrows(MappingException.class, () -> resourceMapper.toResourceResponse(invalidResource));
  }

  @Test
  void willFailToMapInvalidDtoToResource() {
    ResourceRequestDto invalidResourceRequestDto = new ResourceRequestDto();
    assertThrows(
        MappingException.class, () -> resourceMapper.toResourceEntity(invalidResourceRequestDto));
  }

}
