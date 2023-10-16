package jewellery.inventory.unit.mapper;

import jewellery.inventory.dto.request.resource.GemstoneRequestDto;
import jewellery.inventory.mapper.*;
import jewellery.inventory.model.resource.Gemstone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class GemstoneMapperTest {
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

    @Test
    void testGetGemstoneSizeString() {
        Gemstone gemstone = new Gemstone();
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
