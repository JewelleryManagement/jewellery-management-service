package jewellery.inventory.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jewellery.inventory.dto.request.resource.GemstoneRequestDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.MappingException;
import jewellery.inventory.mapper.GemstoneMapper;
import jewellery.inventory.mapper.LinkingPartMapper;
import jewellery.inventory.mapper.PearlMapper;
import jewellery.inventory.mapper.PreciousMetalMapper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Gemstone;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;

class ResourceMapperTest {
  private ResourceMapper resourceMapper;

  @BeforeEach
  void setUp() {
    Locale.setDefault(Locale.US);
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

  @Test
  void testGetGemstoneSizeString() {
    Gemstone gemstone =new Gemstone();
    gemstone.setDimensionX(1.0);
    gemstone.setDimensionY(2.0);
    gemstone.setDimensionZ(3.0);

    String result = resourceMapper.getGemstoneSizeString(gemstone);

    assertEquals("1.0x2.0x3.0", result);
  }

  @Test
  void testSetDimensions() {
    GemstoneRequestDto dto = new GemstoneRequestDto();
    dto.setSize("1.0x2.0x3.0");

    resourceMapper.setDimensions(dto);

    assertEquals(1.0, dto.getDimensionX(), 0.01);
    assertEquals(2.0, dto.getDimensionY(), 0.01);
    assertEquals(3.0, dto.getDimensionZ(), 0.01);
  }

  @Test
  void testSetDimensionsWithNullSize() {
    GemstoneRequestDto dto = new GemstoneRequestDto();
    dto.setSize(null);

    assertThrows(IllegalArgumentException.class, () -> resourceMapper.setDimensions(dto));
  }

  @Test
  void testSetDimensionsWithInvalidFormat() {
    GemstoneRequestDto dto = new GemstoneRequestDto();
    dto.setSize("1.0x2.0x");

    assertThrows(IllegalArgumentException.class, () -> resourceMapper.setDimensions(dto));
  }
}
