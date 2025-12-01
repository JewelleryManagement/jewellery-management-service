package jewellery.inventory.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.MappingException;
import jewellery.inventory.mapper.*;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      PearlMapperImpl.class,
      DiamondMapperImpl.class,
      ColoredStoneMapperImpl.class,
      ColoredStoneMeleeMapperImpl.class,
      ElementMapperImpl.class,
      MetalMapperImpl.class,
      DiamondMeleeMapperImpl.class,
      StringTrimmer.class,
      ResourceMapper.class,
    })
class ResourceMapperTest {
  @Autowired private ResourceMapper resourceMapper;

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
