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
  private static final String CLAZZ_DIAMOND = "Diamond";
  private static final String CLAZZ_DIAMOND_MELEE = "DiamondMelee";
  private static final String CLAZZ_COLORED_STONE = "ColoredStone";
  private static final String CLAZZ_SEMI_PRECIOUS_STONE = "SemiPreciousStone";
  private static final String CLAZZ_METAL = "Metal";
  private static final String CLAZZ_ELEMENT = "Element";
  private static final String QUANTITY_TYPE_UNIT = "unit";
  private static final String QUANTITY_TYPE_WEIGHT = "weight";
  private static final String QUALITY = "AA";
  public static final String UPDATED_QUALITY = QUALITY + "A";
  private static final String SHAPE_PEARL = "oval";
  public static final String UPDATED_SHAPE_PEARL = SHAPE_PEARL + "ish";
  private static final String SHAPE_DIAMOND = "octagon";
  private static final String SHAPE_COLORED_STONE = "Round";
  private static final String SHAPE_SEMI_PRECIOUS_STONE = "Round";
  private static final String SHAPE_SPECIFICATION = "Smooth";
  private static final String COLOR_PEARL = "black";
  public static final String UPDATED_COLOR_PEARL = COLOR_PEARL + "ish";
  private static final String COLOR_METAL = "white";
  public static final String UPDATED_COLOR_METAL = COLOR_METAL + "ish";
  private static final String COLOR_DIAMOND = "ruby";
  public static final String UPDATED_COLOR_DIAMOND = COLOR_DIAMOND + "ish";
  private static final String COLOR_COLORED_STONE = "Blue";
  private static final String COLOR_SEMI_PRECIOUS_STONE = "White";
  private static final String COLOR_HUE = "Yellowish";
  private static final String TYPE_PEARL = "Akoya";
  private static final String TYPE_METAL = "gold";
  private static final String TYPE_DIAMOND = "Natural";
  private static final String TYPE_DIAMOND_MELEE = "Natural";
  private static final String TYPE_COLORED_STONE = "Sapphire";
  private static final String TYPE_SEMI_PRECIOUS_STONE = "Amethyst";
  private static final String CUT_DIAMOND = "diamond";
  private static final String CUT_COLORED_STONE = "Good";
  private static final BigDecimal DIMENSION_X = getBigDecimal("4.5");
  private static final BigDecimal DIMENSION_Y = getBigDecimal("4.9");
  private static final BigDecimal DIMENSION_Z = getBigDecimal("2.5");
  private static final String PEARL_SIZE = "0.55";
  private static final String DIAMOND_MELEE_SIZE = "1.00 - 1.05";
  private static final String COLORED_STONE_MELEE_SIZE = "1.00 - 1.05";
  private static final String SEMI_PRECIOUS_STONE_SIZE = "0.5 - 1.5";
  private static final Integer PURITY = 925;
  public static final int UPDATED_PURITY = PURITY + 1;
  private static final BigDecimal CARAT_DIAMOND = getBigDecimal("5.10000", 5);
  private static final BigDecimal CARAT_DIAMOND_MELEE = getBigDecimal("0.00005", 5);
  public static final BigDecimal UPDATED_CARAT_DIAMOND = CARAT_DIAMOND.add(getBigDecimal("2.1"));
  public static final BigDecimal CARAT_COLORED_STONE = getBigDecimal("1.05", 2);
  private static final String CLARITY = "opaque";
  public static final String UPDATED_CLARITY = CLARITY + "ish";
  private static final String TREATMENT_COLORED_STONE = "None";
  private static final String DESCRIPTION = "A linking part made of gold";
  public static final String UPDATED_DESCRIPTION = DESCRIPTION + " and \"real\" silver";
  public static final BigDecimal PRICE_PER_QUANTITY = getBigDecimal("50.5");
  public static final String NOTE = "Note";
  public static final String PEARL_SKU = UUID.randomUUID().toString();
  public static final String METAL_SKU = UUID.randomUUID().toString();
  public static final String DIAMOND_SKU = UUID.randomUUID().toString();
  public static final String DIAMOND_MELEE_SKU = UUID.randomUUID().toString();
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
        .color(COLOR_METAL)
        .type(TYPE_METAL)
        .purity(PURITY)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .sku(METAL_SKU)
        .build();
  }

  public static Resource getDiamond() {
    return Diamond.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_DIAMOND)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_DIAMOND)
        .carat(CARAT_DIAMOND)
        .color(COLOR_DIAMOND)
        .cut(CUT_DIAMOND)
        .clarity(CLARITY)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .shape(SHAPE_DIAMOND)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .polish(POLISH)
        .symmetry(SYMMETRY)
        .fluorescence(FLUORESCENCE)
        .certificate(CERTIFICATE)
        .sku(DIAMOND_SKU)
        .build();
  }

  public static Resource getDiamondMelee() {
    return DiamondMelee.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_DIAMOND_MELEE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .color(COLOR_DIAMOND)
        .cut(CUT_DIAMOND)
        .clarity(CLARITY)
        .shape(SHAPE_DIAMOND)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .size(DIAMOND_MELEE_SIZE)
        .type(TYPE_DIAMOND_MELEE)
        .carat(CARAT_DIAMOND_MELEE)
        .sku(DIAMOND_MELEE_SKU)
        .build();
  }

  public static Resource getColoredStone() {
    return ColoredStone.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_COLORED_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_COLORED_STONE)
        .shape(SHAPE_COLORED_STONE)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .carat(CARAT_COLORED_STONE)
        .color(COLOR_COLORED_STONE)
        .colorHue(COLOR_HUE)
        .clarity(CLARITY)
        .cut(CUT_COLORED_STONE)
        .treatment(TREATMENT_COLORED_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .certificate(CERTIFICATE)
        .note(NOTE)
        .build();
  }

  public static Resource getColoredStoneMelee() {
    return ColoredStoneMelee.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_COLORED_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_COLORED_STONE)
        .shape(SHAPE_COLORED_STONE)
        .size(COLORED_STONE_MELEE_SIZE)
        .carat(CARAT_COLORED_STONE)
        .color(COLOR_COLORED_STONE)
        .colorHue(COLOR_COLORED_STONE)
        .clarity(CLARITY)
        .cut(CUT_COLORED_STONE)
        .treatment(TREATMENT_COLORED_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .build();
  }

  public static Resource getSemiPreciousStone() {
    return SemiPreciousStone.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_SEMI_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_SEMI_PRECIOUS_STONE)
        .quality(QUALITY)
        .shape(SHAPE_SEMI_PRECIOUS_STONE)
        .shapeSpecification(SHAPE_SPECIFICATION)
        .color(COLOR_SEMI_PRECIOUS_STONE)
        .colorHue(COLOR_HUE)
        .size(SEMI_PRECIOUS_STONE_SIZE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
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
    metalDTO.setPurity(UPDATED_PURITY);
    return metalDTO;
  }

  public static Metal getUpdatedMetal() {
    Metal metal = (Metal) getMetal();
    metal.setColor(UPDATED_COLOR_METAL);
    metal.setPurity(UPDATED_PURITY);
    return metal;
  }

  public static ResourceResponseDto getDiamondResponseDto() {
    return DiamondResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_DIAMOND)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_DIAMOND)
        .carat(CARAT_DIAMOND)
        .color(COLOR_DIAMOND)
        .cut(CUT_DIAMOND)
        .clarity(CLARITY)
        .shape(SHAPE_DIAMOND)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .polish(POLISH)
        .symmetry(SYMMETRY)
        .fluorescence(FLUORESCENCE)
        .certificate(CERTIFICATE)
        .sku(DIAMOND_SKU)
        .build();
  }

  public static ResourceRequestDto getDiamondRequestDto() {
    return DiamondRequestDto.builder()
        .clazz(CLAZZ_DIAMOND)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_DIAMOND)
        .carat(CARAT_DIAMOND)
        .color(COLOR_DIAMOND)
        .cut(CUT_DIAMOND)
        .clarity(CLARITY)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .shape(SHAPE_DIAMOND)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .polish(POLISH)
        .symmetry(SYMMETRY)
        .fluorescence(FLUORESCENCE)
        .certificate(CERTIFICATE)
        .sku(DIAMOND_SKU)
        .build();
  }

  public static DiamondRequestDto getUpdatedDiamondRequestDto() {
    DiamondRequestDto diamondRequestDto = (DiamondRequestDto) getDiamondRequestDto();
    diamondRequestDto.setCarat(UPDATED_CARAT_DIAMOND);
    diamondRequestDto.setClarity(UPDATED_CLARITY);
    diamondRequestDto.setColor(UPDATED_COLOR_DIAMOND);
    return diamondRequestDto;
  }

  private static Resource getUpdatedDiamond() {
    Diamond diamond = (Diamond) getDiamond();
    diamond.setCarat(UPDATED_CARAT_DIAMOND);
    diamond.setClarity(UPDATED_CLARITY);
    diamond.setColor(UPDATED_COLOR_DIAMOND);
    return diamond;
  }

  public static ResourceResponseDto getDiamondMeleeResponseDto() {
    return DiamondMeleeResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_DIAMOND_MELEE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .color(COLOR_DIAMOND)
        .cut(CUT_DIAMOND)
        .clarity(CLARITY)
        .shape(SHAPE_DIAMOND)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .size(DIAMOND_MELEE_SIZE)
        .type(TYPE_DIAMOND_MELEE)
        .carat(CARAT_DIAMOND_MELEE)
        .sku(DIAMOND_MELEE_SKU)
        .build();
  }

  public static ResourceRequestDto getDiamondMeleeRequestDto() {
    return DiamondMeleeRequestDto.builder()
        .clazz(CLAZZ_DIAMOND_MELEE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .color(COLOR_DIAMOND)
        .cut(CUT_DIAMOND)
        .clarity(CLARITY)
        .shape(SHAPE_DIAMOND)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .size(DIAMOND_MELEE_SIZE)
        .type(TYPE_DIAMOND_MELEE)
        .carat(CARAT_DIAMOND_MELEE)
        .sku(DIAMOND_MELEE_SKU)
        .build();
  }

  public static DiamondMeleeRequestDto getUpdatedDiamondMeleeRequestDto() {
    DiamondMeleeRequestDto diamondMeleeRequestDto =
        (DiamondMeleeRequestDto) getDiamondMeleeRequestDto();
    diamondMeleeRequestDto.setClarity(UPDATED_CLARITY);
    diamondMeleeRequestDto.setColor(UPDATED_COLOR_DIAMOND);
    return diamondMeleeRequestDto;
  }

  private static Resource getUpdatedDiamondMelee() {
    DiamondMelee diamondMelee = (DiamondMelee) getDiamondMelee();
    diamondMelee.setClarity(UPDATED_CLARITY);
    diamondMelee.setColor(UPDATED_COLOR_DIAMOND);
    return diamondMelee;
  }

  public static ResourceResponseDto getColoredStoneResponseDto() {
    return ColoredStoneResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_COLORED_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_COLORED_STONE)
        .shape(SHAPE_COLORED_STONE)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .carat(CARAT_COLORED_STONE)
        .color(COLOR_COLORED_STONE)
        .colorHue(COLOR_HUE)
        .clarity(CLARITY)
        .cut(CUT_COLORED_STONE)
        .treatment(TREATMENT_COLORED_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .certificate(CERTIFICATE)
        .note(NOTE)
        .build();
  }

  public static ResourceRequestDto getColoredStoneRequestDto() {
    return ColoredStoneRequestDto.builder()
        .clazz(CLAZZ_COLORED_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_COLORED_STONE)
        .shape(SHAPE_COLORED_STONE)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .carat(CARAT_COLORED_STONE)
        .color(COLOR_COLORED_STONE)
        .colorHue(COLOR_HUE)
        .clarity(CLARITY)
        .cut(CUT_COLORED_STONE)
        .treatment(TREATMENT_COLORED_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .certificate(CERTIFICATE)
        .note(NOTE)
        .build();
  }

  public static ResourceResponseDto getColoredStoneMeleeResponseDto() {
    return ColoredStoneMeleeResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_COLORED_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_COLORED_STONE)
        .shape(SHAPE_COLORED_STONE)
        .size(COLORED_STONE_MELEE_SIZE)
        .carat(CARAT_COLORED_STONE)
        .color(COLOR_COLORED_STONE)
        .colorHue(COLOR_COLORED_STONE)
        .clarity(CLARITY)
        .cut(CUT_COLORED_STONE)
        .treatment(TREATMENT_COLORED_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .build();
  }

  public static ResourceRequestDto getColoredStoneMeleeRequestDto() {
    return ColoredStoneMeleeRequestDto.builder()
        .clazz(CLAZZ_COLORED_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_COLORED_STONE)
        .shape(SHAPE_COLORED_STONE)
        .size(COLORED_STONE_MELEE_SIZE)
        .carat(CARAT_COLORED_STONE)
        .color(COLOR_COLORED_STONE)
        .colorHue(COLOR_COLORED_STONE)
        .clarity(CLARITY)
        .cut(CUT_COLORED_STONE)
        .treatment(TREATMENT_COLORED_STONE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .build();
  }

  public static ResourceResponseDto getSemiPreciousStoneResponseDto() {
    return SemiPreciousStoneResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_SEMI_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_SEMI_PRECIOUS_STONE)
        .quality(QUALITY)
        .shape(SHAPE_SEMI_PRECIOUS_STONE)
        .shapeSpecification(SHAPE_SPECIFICATION)
        .color(COLOR_SEMI_PRECIOUS_STONE)
        .colorHue(COLOR_HUE)
        .size(SEMI_PRECIOUS_STONE_SIZE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .build();
  }

  public static ResourceRequestDto getSemiPreciousStoneRequestDto() {
    return SemiPreciousStoneRequestDto.builder()
        .clazz(CLAZZ_SEMI_PRECIOUS_STONE)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_SEMI_PRECIOUS_STONE)
        .quality(QUALITY)
        .shape(SHAPE_SEMI_PRECIOUS_STONE)
        .shapeSpecification(SHAPE_SPECIFICATION)
        .color(COLOR_SEMI_PRECIOUS_STONE)
        .colorHue(COLOR_HUE)
        .size(SEMI_PRECIOUS_STONE_SIZE)
        .pricePerQuantity(PRICE_PER_QUANTITY)
        .note(NOTE)
        .build();
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
        Arguments.of(getDiamond(), getDiamondRequestDto()),
        Arguments.of(getDiamondMelee(), getDiamondMeleeRequestDto()),
        Arguments.of(getColoredStone(), getColoredStoneRequestDto()),
        Arguments.of(getColoredStoneMelee(), getColoredStoneMeleeRequestDto()),
        Arguments.of(getSemiPreciousStone(), getSemiPreciousStoneRequestDto()),
        Arguments.of(getElement(), getElementRequestDto()),
        Arguments.of(getPearl(), getPearlRequestDto()),
        Arguments.of(getMetal(), getMetalRequestDto()));
  }

  public static Stream<Arguments> provideResourcesAndResponseDtos() {
    return Stream.of(
        Arguments.of(getDiamond(), getDiamondResponseDto()),
        Arguments.of(getDiamondMelee(), getDiamondMeleeResponseDto()),
        Arguments.of(getColoredStone(), getColoredStoneResponseDto()),
        Arguments.of(getColoredStoneMelee(), getColoredStoneMeleeResponseDto()),
        Arguments.of(getSemiPreciousStone(), getSemiPreciousStoneResponseDto()),
        Arguments.of(getElement(), getElementResponseDto()),
        Arguments.of(getPearl(), getPearlResponseDto()),
        Arguments.of(getPearlWithNullFields(), getPearlResponseDtoWithNullFields()),
        Arguments.of(getMetal(), getMetalResponseDto()));
  }

  public static Stream<Arguments> provideUpdatedResourcesAndUpdatedRequestDtos() {
    return Stream.of(
        Arguments.of(getUpdatedDiamond(), getUpdatedDiamondRequestDto()),
        Arguments.of(getUpdatedDiamondMelee(), getUpdatedDiamondMeleeRequestDto()),
        Arguments.of(getUpdatedElement(), getUpdatedElementRequestDto()),
        Arguments.of(getUpdatedPearl(), getUpdatedPearlRequestDto()),
        Arguments.of(getUpdatedMetal(), getUpdatedMetalRequestDto()));
  }

  public static Stream<ResourceRequestDto> provideUpdatedResourceRequestDtos() {
    return Stream.of(
        getUpdatedDiamondRequestDto(),
        getUpdatedDiamondMeleeRequestDto(),
        getUpdatedElementRequestDto(),
        getUpdatedPearlRequestDto(),
        getUpdatedMetalRequestDto());
  }

  public static Stream<ResourceRequestDto> provideResourceRequestDtos() {
    return Stream.of(
        getDiamondRequestDto(),
        getDiamondMeleeRequestDto(),
        getElementRequestDto(),
        getPearlRequestDto(),
        getMetalRequestDto());
  }

  public static Stream<Resource> provideResources() {
    return Stream.of(getDiamond(), getDiamondMelee(), getElement(), getPearl(), getMetal());
  }
}
