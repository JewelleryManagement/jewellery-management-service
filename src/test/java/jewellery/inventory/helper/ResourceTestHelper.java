package jewellery.inventory.helper;

import java.util.UUID;
import java.util.stream.Stream;
import jewellery.inventory.dto.request.resource.*;
import jewellery.inventory.dto.response.resource.*;
import jewellery.inventory.model.resource.*;
import org.junit.jupiter.params.provider.Arguments;

public class ResourceTestHelper {
  private static final UUID RESOURCE_ID = UUID.randomUUID();
  private static final String CLAZZ_PEARL = "Pearl";
  private static final String CLAZZ_GEMSTONE = "Gemstone";
  private static final String CLAZZ_PRECIOUS_METAL = "PreciousMetal";
  private static final String CLAZZ_LINKING_PART = "LinkingPart";
  private static final String QUANTITY_TYPE_UNIT = "unit";
  private static final String QUANTITY_TYPE_WEIGHT = "weight";
  private static final String QUALITY = "AA";
  public static final String UPDATED_QUALITY = QUALITY + "A";
  private static final String SHAPE_PEARL = "oval";
  public static final String UPDATED_SHAPE_PEARL = SHAPE_PEARL + "ish";
  private static final String SHAPE_GEMSTONEL = "octagon";
  private static final String COLOR_PEARL = "black";
  public static final String UPDATED_COLOR_PEARL = COLOR_PEARL + "ish";
  private static final String COLOR_METAL = "white";
  public static final String UPDATED_COLOR_METAL = COLOR_METAL + "ish";
  private static final String COLOR_GEMSTONE = "ruby";
  public static final String UPDATED_COLOR_GEMSTONE = COLOR_GEMSTONE + "ish";
  private static final String TYPE_PEARL = "Akoya";
  private static final String TYPE_METAL = "gold";
  private static final String PLATING = "silver";
  public static final String UPDATED_PLATING = PLATING + "ish";
  private static final String CUT = "diamond";
  private static final String DIMENSION_SIZE="4,50x4,90x2,50";
  private static final Double DIMENSION_X = 4.5;
  private static final Double DIMENSION_Y = 4.9;
  private static final Double DIMENSION_Z = 2.5;
  private static final Double SIZE = 0.55;
  private static final Integer PURITY = 925;
  public static final int UPDATED_PURITY = PURITY + 1;
  private static final Double CARAT_GEMSTONE = 5.0;
  public static final double UPDATED_CARAT_GEMSTONE = CARAT_GEMSTONE + 2;
  private static final String CLARITY = "opaque";
  public static final String UPDATED_CLARITY = CLARITY + "ish";
  private static final String DESCRIPTION = "A linking part made of gold";
  public static final String UPDATED_DESCRIPTION = DESCRIPTION + " and \"real\" silver";

  private ResourceTestHelper() {}

  public static Resource getPearl() {
    return Pearl.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PEARL)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_PEARL)
        .size(SIZE)
        .quality(QUALITY)
        .color(COLOR_PEARL)
        .shape(SHAPE_PEARL)
        .build();
  }

  public static Resource getPreciousMetal() {
    return PreciousMetal.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PRECIOUS_METAL)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_METAL)
        .plating(PLATING)
        .color(COLOR_METAL)
        .type(TYPE_METAL)
        .purity(PURITY)
        .build();
  }

  public static Resource getGemstone() {
    return Gemstone.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_GEMSTONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .carat(CARAT_GEMSTONE)
        .color(COLOR_GEMSTONE)
        .cut(CUT)
        .clarity(CLARITY)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .shape(SHAPE_GEMSTONEL)
        .build();
  }

  public static Resource getLinkingPart() {
    return LinkingPart.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_LINKING_PART)
        .quantityType(QUANTITY_TYPE_UNIT)
        .description(DESCRIPTION)
        .build();
  }

  public static ResourceResponseDto getPearlResponseDto() {
    return PearlResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PEARL)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_PEARL)
        .size(SIZE)
        .quality(QUALITY)
        .color(COLOR_PEARL)
        .shape(SHAPE_PEARL)
        .build();
  }

  private static ResourceRequestDto getPearlRequestDto() {
    return PearlRequestDto.builder()
        .clazz(CLAZZ_PEARL)
        .quantityType(QUANTITY_TYPE_UNIT)
        .type(TYPE_PEARL)
        .size(SIZE)
        .quality(QUALITY)
        .color(COLOR_PEARL)
        .shape(SHAPE_PEARL)
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

  public static ResourceResponseDto getPreciousMetalResponseDto() {
    return PreciousMetalResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_PRECIOUS_METAL)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_METAL)
        .plating(PLATING)
        .color(COLOR_METAL)
        .type(TYPE_METAL)
        .purity(PURITY)
        .build();
  }

  private static ResourceRequestDto getPreciousMetalRequestDto() {
    return PreciousMetalRequestDto.builder()
        .clazz(CLAZZ_PRECIOUS_METAL)
        .quantityType(QUANTITY_TYPE_WEIGHT)
        .type(TYPE_METAL)
        .plating(PLATING)
        .color(COLOR_METAL)
        .type(TYPE_METAL)
        .purity(PURITY)
        .build();
  }

  public static PreciousMetalRequestDto getUpdatedPreciousMetalRequestDto() {
    PreciousMetalRequestDto preciousMetalDTO =
        (PreciousMetalRequestDto) getPreciousMetalRequestDto();
    preciousMetalDTO.setColor(UPDATED_COLOR_METAL);
    preciousMetalDTO.setPlating(UPDATED_PLATING);
    preciousMetalDTO.setPurity(UPDATED_PURITY);
    return preciousMetalDTO;
  }

  public static PreciousMetal getUpdatedPreciousMetal() {
    PreciousMetal preciousMetal = (PreciousMetal) getPreciousMetal();
    preciousMetal.setColor(UPDATED_COLOR_METAL);
    preciousMetal.setPlating(UPDATED_PLATING);
    preciousMetal.setPurity(UPDATED_PURITY);
    return preciousMetal;
  }

  public static ResourceResponseDto getGemstoneResponseDto() {
    return GemstoneResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_GEMSTONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .carat(CARAT_GEMSTONE)
        .color(COLOR_GEMSTONE)
        .cut(CUT)
        .clarity(CLARITY)
            .size(DIMENSION_SIZE)
        .shape(SHAPE_GEMSTONEL)
        .build();
  }

  public static ResourceRequestDto getGemstoneRequestDto() {
    return GemstoneRequestDto.builder()
        .clazz(CLAZZ_GEMSTONE)
        .quantityType(QUANTITY_TYPE_UNIT)
        .carat(CARAT_GEMSTONE)
        .color(COLOR_GEMSTONE)
        .cut(CUT)
        .clarity(CLARITY)
        .dimensionX(DIMENSION_X)
        .dimensionY(DIMENSION_Y)
        .dimensionZ(DIMENSION_Z)
        .shape(SHAPE_GEMSTONEL)
        .build();
  }

  public static GemstoneRequestDto getUdpatedGemstoneRequestDto() {
    GemstoneRequestDto gemstoneDTO = (GemstoneRequestDto) getGemstoneRequestDto();
    gemstoneDTO.setCarat(UPDATED_CARAT_GEMSTONE);
    gemstoneDTO.setClarity(UPDATED_CLARITY);
    gemstoneDTO.setColor(UPDATED_COLOR_GEMSTONE);
    return gemstoneDTO;
  }

  private static Resource getUpdatedGemstone() {
    Gemstone gemstone = (Gemstone) getGemstone();
    gemstone.setCarat(UPDATED_CARAT_GEMSTONE);
    gemstone.setClarity(UPDATED_CLARITY);
    gemstone.setColor(UPDATED_COLOR_GEMSTONE);
    return gemstone;
  }

  public static ResourceResponseDto getLinkingPartResponseDto() {
    return LinkingPartResponseDto.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_LINKING_PART)
        .quantityType(QUANTITY_TYPE_UNIT)
        .description(DESCRIPTION)
        .build();
  }

  private static ResourceRequestDto getLinkingPartRequestDto() {
    return LinkingPartRequestDto.builder()
        .clazz(CLAZZ_LINKING_PART)
        .quantityType(QUANTITY_TYPE_UNIT)
        .description(DESCRIPTION)
        .build();
  }

  public static LinkingPartRequestDto getUpdatedLinkingPartRequestDto() {
    LinkingPartRequestDto linkingPartDTO = (LinkingPartRequestDto) getLinkingPartRequestDto();
    linkingPartDTO.setDescription(UPDATED_DESCRIPTION);
    return linkingPartDTO;
  }

  public static LinkingPart getUpdatedLinkingPart() {
    LinkingPart linkingPart = (LinkingPart) getLinkingPart();
    linkingPart.setDescription(UPDATED_DESCRIPTION);
    return linkingPart;
  }

  public static Stream<Arguments> provideResourcesAndRequestDtos() {
    return Stream.of(
        Arguments.of(getGemstone(), getGemstoneRequestDto()),
        Arguments.of(getLinkingPart(), getLinkingPartRequestDto()),
        Arguments.of(getPearl(), getPearlRequestDto()),
        Arguments.of(getPreciousMetal(), getPreciousMetalRequestDto()));
  }

  public static Stream<Arguments> provideResourcesAndResponseDtos() {
    return Stream.of(
        Arguments.of(getGemstone(), getGemstoneResponseDto()),
        Arguments.of(getLinkingPart(), getLinkingPartResponseDto()),
        Arguments.of(getPearl(), getPearlResponseDto()),
        Arguments.of(getPreciousMetal(), getPreciousMetalResponseDto()));
  }

  public static Stream<Arguments> provideUpdatedResourcesAndUpdatedRequestDtos() {
    return Stream.of(
        Arguments.of(getUpdatedGemstone(), getUdpatedGemstoneRequestDto()),
        Arguments.of(getUpdatedLinkingPart(), getUpdatedLinkingPartRequestDto()),
        Arguments.of(getUpdatedPearl(), getUpdatedPearlRequestDto()),
        Arguments.of(getUpdatedPreciousMetal(), getUpdatedPreciousMetalRequestDto()));
  }

  public static Stream<ResourceRequestDto> provideUpdatedResourceRequestDtos() {
    return Stream.of(
        getUdpatedGemstoneRequestDto(),
        getUpdatedLinkingPartRequestDto(),
        getUpdatedPearlRequestDto(),
        getUpdatedPreciousMetalRequestDto());
  }

  public static Stream<ResourceRequestDto> provideResourceRequestDtos() {
    return Stream.of(
        getGemstoneRequestDto(),
        getLinkingPartRequestDto(),
        getPearlRequestDto(),
        getPreciousMetalRequestDto());
  }

  public static Stream<Resource> provideResources() {
    return Stream.of(getGemstone(), getLinkingPart(), getPearl(), getPreciousMetal());
  }
}
