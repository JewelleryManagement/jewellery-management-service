package jewellery.inventory.helper;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import jewellery.inventory.dto.request.resource.*;
import jewellery.inventory.dto.response.resource.*;
import jewellery.inventory.model.resource.*;
import org.junit.jupiter.params.provider.Arguments;

public class ResourceTestHelper {
  private static final UUID RESOURCE_ID = UUID.randomUUID();
  private static final String CLAZZ_PEARL = "Pearl";
  private static final String CLAZZ_PRECIOUS_STONE = "PreciousStone";
  private static final String CLAZZ_SEMI_PRECIOUS_STONE = "SemiPreciousStone";
  private static final String CLAZZ_METAL = "Metal";
  private static final String CLAZZ_ELEMENT = "Element";
  private static final String QUANTITY_TYPE_UNIT = "unit";
  private static final String QUANTITY_TYPE_WEIGHT = "weight";
  private static final String QUALITY = "AA";
  public static final String UPDATED_QUALITY = QUALITY + "A";
  private static final String SHAPE_PEARL = "oval";
  public static final String UPDATED_SHAPE_PEARL = SHAPE_PEARL + "ish";
  private static final String SHAPE_PRECIOUS_STONE = "octagon";
  private static final String COLOR_PEARL = "black";
  public static final String UPDATED_COLOR_PEARL = COLOR_PEARL + "ish";
  private static final String COLOR_METAL = "white";
  public static final String UPDATED_COLOR_METAL = COLOR_METAL + "ish";
  private static final String COLOR_PRECIOUS_STONE = "ruby";
  public static final String UPDATED_COLOR_PRECIOUS_STONE = COLOR_PRECIOUS_STONE + "ish";
  private static final String TYPE_PEARL = "Akoya";
  private static final String TYPE_METAL = "gold";
  private static final String PLATING = "silver";
  public static final String UPDATED_PLATING = PLATING + "ish";
  private static final String CUT = "diamond";
  private static final String DIMENSION_SIZE = "4.50x4.90x2.50";
  private static final BigDecimal DIMENSION_X = getBigDecimal("4.5");
  private static final BigDecimal DIMENSION_Y = getBigDecimal("4.9");
  private static final BigDecimal DIMENSION_Z = getBigDecimal("2.5");
  private static final BigDecimal SIZE = getBigDecimal("0.55");
  private static final String PEARL_SIZE = "0.55";
  private static final Integer PURITY = 925;
  public static final int UPDATED_PURITY = PURITY + 1;
  private static final BigDecimal CARAT_PRECIOUS_STONE = getBigDecimal("5.1");
  public static final BigDecimal UPDATED_CARAT_PRECIOUS_STONE =
      CARAT_PRECIOUS_STONE.add(getBigDecimal("2.1"));
  private static final String CLARITY = "opaque";
  public static final String UPDATED_CLARITY = CLARITY + "ish";
  private static final String DESCRIPTION = "A linking part made of gold";
  public static final String UPDATED_DESCRIPTION = DESCRIPTION + " and \"real\" silver";
  public static final BigDecimal PRICE_PER_QUANTITY = getBigDecimal("50.5");
  public static final String NOTE = "Note";
  public static final String PEARL_SKU = UUID.randomUUID().toString();
  public static final String METAL_SKU = UUID.randomUUID().toString();
  public static final String PRECIOUS_STONE_SKU = UUID.randomUUID().toString();
  public static final String SEMI_PRECIOUS_STONE_SKU = UUID.randomUUID().toString();
  public static final String ELEMENT_SKU = UUID.randomUUID().toString();
  public static final String POLISH = "Good";
  public static final String SYMMETRY = "Good";
  public static final String FLUORESCENCE = "Good";
  public static final String CERTIFICATE = "None";

  private ResourceTestHelper() {}

  public static Resource getPearl() {
    return getPearl(PRICE_PER_QUANTITY);
  }

  public static Resource getPearlWithNullFields() {
    return Pearl.builder().id(RESOURCE_ID).clazz(CLAZZ_PEARL).build();
  }

  public static Resource getPearl(BigDecimal price) {
    return Pearl.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PEARL)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_PEARL)
        .size(PEARL_SIZE)
        .quality(QUALITY)
        .color(COLOR_PEARL)
        .shape(SHAPE_PEARL)
        .pricePerQuantity(price)
        .note(NOTE)
        .sku(PEARL_SKU)
        .build();
  }

  public static Resource getMetal() {
    return Metal.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_METAL)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_METAL)
        .plating(PLATING)
        .color(COLOR_METAL)
        .type(TYPE_METAL)
        .purity(PURITY)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(METAL_SKU)
        .build();
  }

  public static Resource getPreciousStone() {
    return PreciousStone.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .carat(CARAT_PRECIOUS_STONE)
        .color(COLOR_PRECIOUS_STONE)
        .cut(CUT)
        .clarity(CLARITY)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .shape(SHAPE_PRECIOUS_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .polish(POLISH)
        .symmetry(SYMMETRY)
        .fluorescence(FLUORESCENCE)
        .certificate(CERTIFICATE)
        .sku(PRECIOUS_STONE_SKU)
        .build();
  }

  public static Resource getSemiPreciousStone() {
    return SemiPreciousStone.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_SEMI_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .color(COLOR_PRECIOUS_STONE)
        .cut(CUT)
        .clarity(CLARITY)
        .shape(SHAPE_PRECIOUS_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .size(SIZE)
        .sku(SEMI_PRECIOUS_STONE_SKU)
        .build();
  }

  public static Resource getElement() {
    return Element.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_ELEMENT)
        .quantityType(QUANTITY_TYPE_UNIT)
        .description(DESCRIPTION)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(ELEMENT_SKU)
        .build();
  }

  public static ResourceResponseDto getPearlResponseDto() {
    return PearlResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PEARL)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_PEARL)
        .size(PEARL_SIZE)
        .quality(QUALITY)
        .color(COLOR_PEARL)
        .shape(SHAPE_PEARL)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(PEARL_SKU)
        .build();
  }

  public static ResourceResponseDto getPearlResponseDtoWithNullFields() {
    return PearlResponseDto.builder().id(RESOURCE_ID).clazz(CLAZZ_PEARL).build();
  }

  public static ResourceRequestDto getPearlRequestDto() {
    return PearlRequestDto.builder()
        .clazz(CLAZZ_PEARL)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_PEARL)
        .size(PEARL_SIZE)
        .quality(QUALITY)
        .color(COLOR_PEARL)
        .shape(SHAPE_PEARL)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(PEARL_SKU)
        .build();
  }

  public static PearlRequestDto getUpdatedPearlRequestDto() {
    PearlRequestDto pearlDTO = (PearlRequestDto) getPearlRequestDto();
    pearlDTO.setColor(UPDATED_COLOR_PEARL);
    pearlDTO.setQuality(UPDATED_QUALITY);
    pearlDTO.setShape(UPDATED_SHAPE_PEARL);
    return pearlDTO;
  }

  public static Pearl getUpdatedPearl() {
    Pearl pearl = (Pearl) getPearl();
    pearl.setColor(UPDATED_COLOR_PEARL);
    pearl.setQuality(UPDATED_QUALITY);
    pearl.setShape(UPDATED_SHAPE_PEARL);
    return pearl;
  }

  public static ResourceResponseDto getMetalResponseDto() {
    return MetalResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_METAL)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_METAL)
        .plating(PLATING)
        .color(COLOR_METAL)
        .type(TYPE_METAL)
        .purity(PURITY)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(METAL_SKU)
        .build();
  }

  public static ResourceRequestDto getMetalRequestDto() {
    return MetalRequestDto.builder()
        .clazz(CLAZZ_METAL)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_METAL)
        .plating(PLATING)
        .color(COLOR_METAL)
        .type(TYPE_METAL)
        .purity(PURITY)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(METAL_SKU)
        .build();
  }

  public static MetalRequestDto getUpdatedMetalRequestDto() {
    MetalRequestDto metalDTO = (MetalRequestDto) getMetalRequestDto();
    metalDTO.setColor(UPDATED_COLOR_METAL);
    metalDTO.setPlating(UPDATED_PLATING);
    metalDTO.setPurity(UPDATED_PURITY);
    return metalDTO;
  }

  public static Metal getUpdatedMetal() {
    Metal metal = (Metal) getMetal();
    metal.setColor(UPDATED_COLOR_METAL);
    metal.setPlating(UPDATED_PLATING);
    metal.setPurity(UPDATED_PURITY);
    return metal;
  }

  public static ResourceResponseDto getPreciousStoneResponseDto() {
    return PreciousStoneResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .carat(CARAT_PRECIOUS_STONE)
        .color(COLOR_PRECIOUS_STONE)
        .cut(CUT)
        .clarity(CLARITY)
        .shape(SHAPE_PRECIOUS_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .size(DIMENSION_SIZE)
        .polish(POLISH)
        .symmetry(SYMMETRY)
        .fluorescence(FLUORESCENCE)
        .certificate(CERTIFICATE)
        .sku(PRECIOUS_STONE_SKU)
        .build();
  }

  public static ResourceRequestDto getPreciousStoneRequestDto() {
    return PreciousStoneRequestDto.builder()
        .clazz(CLAZZ_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .carat(CARAT_PRECIOUS_STONE)
        .color(COLOR_PRECIOUS_STONE)
        .cut(CUT)
        .clarity(CLARITY)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .shape(SHAPE_PRECIOUS_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .polish(POLISH)
        .symmetry(SYMMETRY)
        .fluorescence(FLUORESCENCE)
        .certificate(CERTIFICATE)
        .sku(PRECIOUS_STONE_SKU)
        .build();
  }

  public static PreciousStoneRequestDto getUpdatedPreciousStoneRequestDto() {
    PreciousStoneRequestDto preciousStoneRequestDto =
        (PreciousStoneRequestDto) getPreciousStoneRequestDto();
    preciousStoneRequestDto.setCarat(UPDATED_CARAT_PRECIOUS_STONE);
    preciousStoneRequestDto.setClarity(UPDATED_CLARITY);
    preciousStoneRequestDto.setColor(UPDATED_COLOR_PRECIOUS_STONE);
    return preciousStoneRequestDto;
  }

  private static Resource getUpdatedPreciousStone() {
    PreciousStone preciousStone = (PreciousStone) getPreciousStone();
    preciousStone.setCarat(UPDATED_CARAT_PRECIOUS_STONE);
    preciousStone.setClarity(UPDATED_CLARITY);
    preciousStone.setColor(UPDATED_COLOR_PRECIOUS_STONE);
    return preciousStone;
  }

  public static ResourceResponseDto getSemiPreciousStoneResponseDto() {
    return SemiPreciousStoneResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_SEMI_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .color(COLOR_PRECIOUS_STONE)
        .cut(CUT)
        .clarity(CLARITY)
        .shape(SHAPE_PRECIOUS_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .size(SIZE)
        .sku(SEMI_PRECIOUS_STONE_SKU)
        .build();
  }

  public static ResourceRequestDto getSemiPreciousStoneRequestDto() {
    return SemiPreciousStoneRequestDto.builder()
        .clazz(CLAZZ_SEMI_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .color(COLOR_PRECIOUS_STONE)
        .cut(CUT)
        .clarity(CLARITY)
        .shape(SHAPE_PRECIOUS_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .size(SIZE)
        .sku(SEMI_PRECIOUS_STONE_SKU)
        .build();
  }

  public static SemiPreciousStoneRequestDto getUpdatedSemiPreciousStoneRequestDto() {
    SemiPreciousStoneRequestDto semiPreciousStoneRequestDto =
        (SemiPreciousStoneRequestDto) getSemiPreciousStoneRequestDto();
    semiPreciousStoneRequestDto.setClarity(UPDATED_CLARITY);
    semiPreciousStoneRequestDto.setColor(UPDATED_COLOR_PRECIOUS_STONE);
    return semiPreciousStoneRequestDto;
  }

  private static Resource getUpdatedSemiPreciousStone() {
    SemiPreciousStone semiPreciousStone = (SemiPreciousStone) getSemiPreciousStone();
    semiPreciousStone.setClarity(UPDATED_CLARITY);
    semiPreciousStone.setColor(UPDATED_COLOR_PRECIOUS_STONE);
    return semiPreciousStone;
  }

  public static ResourceResponseDto getElementResponseDto() {
    return ElementResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_ELEMENT)
        .quantityType(QUANTITY_TYPE_UNIT)
        .description(DESCRIPTION)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(ELEMENT_SKU)
        .build();
  }

  public static ResourceRequestDto getElementRequestDto() {
    return ElementRequestDto.builder()
        .clazz(CLAZZ_ELEMENT)
        .quantityType(QUANTITY_TYPE_UNIT)
        .description(DESCRIPTION)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(ELEMENT_SKU)
        .build();
  }

  public static ElementRequestDto getUpdatedElementRequestDto() {
    ElementRequestDto linkingPartDTO = (ElementRequestDto) getElementRequestDto();
    linkingPartDTO.setDescription(UPDATED_DESCRIPTION);
    return linkingPartDTO;
  }

  public static Element getUpdatedElement() {
    Element element = (Element) getElement();
    element.setDescription(UPDATED_DESCRIPTION);
    return element;
  }

  public static Stream<Arguments> provideResourcesAndRequestDtos() {
    return Stream.of(
        Arguments.of(getPreciousStone(), getPreciousStoneRequestDto()),
        Arguments.of(getSemiPreciousStone(), getSemiPreciousStoneRequestDto()),
        Arguments.of(getElement(), getElementRequestDto()),
        Arguments.of(getPearl(), getPearlRequestDto()),
        Arguments.of(getMetal(), getMetalRequestDto()));
  }

  public static Stream<Arguments> provideResourcesAndResponseDtos() {
    return Stream.of(
        Arguments.of(getPreciousStone(), getPreciousStoneResponseDto()),
        Arguments.of(getSemiPreciousStone(), getSemiPreciousStoneResponseDto()),
        Arguments.of(getElement(), getElementResponseDto()),
        Arguments.of(getPearl(), getPearlResponseDto()),
        Arguments.of(getPearlWithNullFields(), getPearlResponseDtoWithNullFields()),
        Arguments.of(getMetal(), getMetalResponseDto()));
  }

  public static Stream<Arguments> provideUpdatedResourcesAndUpdatedRequestDtos() {
    return Stream.of(
        Arguments.of(getUpdatedPreciousStone(), getUpdatedPreciousStoneRequestDto()),
        Arguments.of(getUpdatedSemiPreciousStone(), getUpdatedSemiPreciousStoneRequestDto()),
        Arguments.of(getUpdatedElement(), getUpdatedElementRequestDto()),
        Arguments.of(getUpdatedPearl(), getUpdatedPearlRequestDto()),
        Arguments.of(getUpdatedMetal(), getUpdatedMetalRequestDto()));
  }

  public static Stream<ResourceRequestDto> provideUpdatedResourceRequestDtos() {
    return Stream.of(
        getUpdatedPreciousStoneRequestDto(),
        getUpdatedSemiPreciousStoneRequestDto(),
        getUpdatedElementRequestDto(),
        getUpdatedPearlRequestDto(),
        getUpdatedMetalRequestDto());
  }

  public static Stream<ResourceRequestDto> provideResourceRequestDtos() {
    return Stream.of(
        getPreciousStoneRequestDto(),
        getSemiPreciousStoneRequestDto(),
        getElementRequestDto(),
        getPearlRequestDto(),
        getMetalRequestDto());
  }

  public static Stream<Resource> provideResources() {
    return Stream.of(
        getPreciousStone(), getSemiPreciousStone(), getElement(), getPearl(), getMetal());
  }
}
