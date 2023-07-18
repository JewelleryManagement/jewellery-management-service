package jewellery.inventory.util;

import jewellery.inventory.dto.*;
import jewellery.inventory.model.resource.*;
import org.junit.jupiter.params.provider.Arguments;

import java.util.UUID;
import java.util.stream.Stream;

public class TestUtil {
  private static final UUID RESOURCE_ID = UUID.randomUUID();
  private static final String CLAZZ_PEARL = "Pearl";
  private static final String CLAZZ_GEMSTONE = "Gemstone";
  private static final String CLAZZ_PRECIOUS_METAL = "PreciousMetal";
  private static final String CLAZZ_LINKING_PART = "LinkingPart";
  private static final String QUANTITY_TYPE_UNIT = "unit";
  private static final String QUANTITY_TYPE_WEIGHT = "weight";
  private static final String QUANTITY_TYPE_LENGTH = "length";
  private static final String QUALITY = "AA";
  private static final String SHAPE_PEARL = "oval";
  private static final String SHAPE_GEMSTONEL = "octagon";
  private static final String COLOR_PEARL = "black";
  private static final String COLOR_METAL = "white";
  private static final String COLOR_GEMSTONE = "ruby";
  private static final String TYPE_PEARL = "Akoya";
  private static final String TYPE_METAL = "gold";
  private static final String PLATING = "silver";
  private static final String CUT = "diamond";
  private static final Double DIMENSION_X = 4.5;
  private static final Double DIMENSION_Y = 4.9;
  private static final Double DIMENSION_Z = 2.5;
  private static final Double SIZE = 0.55;
  private static final Integer PURITY = 925;
  private static final Double CARAT_GEMSTONE = 5.0;
  private static final String CLARITY = "opaque";
  private static final String DESCRIPTION = "A linking part made of gold";

  private TestUtil() {}

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

  public static ResourceDTO getPearlDTO() {
    return PearlDTO.builder()
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

  public static PearlDTO getUpdatedPearlDTO() {
    PearlDTO pearlDTO = (PearlDTO) getPearlDTO();
    pearlDTO.setColor(COLOR_PEARL + "ish");
    pearlDTO.setQuality(QUALITY + "A");
    pearlDTO.setShape(SHAPE_PEARL + "ish");
    return pearlDTO;
  }

  public static ResourceDTO getPreciousMetalDTO() {
    return PreciousMetalDTO.builder()
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

  public static PreciousMetalDTO getUpdatedPreciousMetalDTO() {
    PreciousMetalDTO preciousMetalDTO = (PreciousMetalDTO) getPreciousMetalDTO();
    preciousMetalDTO.setColor(COLOR_METAL + "ish");
    preciousMetalDTO.setPlating(PLATING + "ish");
    preciousMetalDTO.setPurity(PURITY + 1);
    return preciousMetalDTO;
  }

  public static ResourceDTO getGemstoneDTO() {
    return GemstoneDTO.builder()
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

  public static GemstoneDTO getUdpatedGemstoneDTO() {
    GemstoneDTO gemstoneDTO = (GemstoneDTO) getGemstoneDTO();
    gemstoneDTO.setCarat(CARAT_GEMSTONE + 2);
    gemstoneDTO.setClarity(CLARITY + "ish");
    gemstoneDTO.setColor(COLOR_GEMSTONE + "ish");
    return gemstoneDTO;
  }

  public static ResourceDTO getLinkingPartDTO() {
    return LinkingPartDTO.builder()
        .id(RESOURCE_ID)
        .clazz(CLAZZ_LINKING_PART)
        .quantityType(QUANTITY_TYPE_UNIT)
        .description(DESCRIPTION)
        .build();
  }

  public static LinkingPartDTO getUpdatedLinkingPartDTO() {
    LinkingPartDTO linkingPartDTO = (LinkingPartDTO) getLinkingPartDTO();
    linkingPartDTO.setDescription(DESCRIPTION + " and \"real\" silver");
    return linkingPartDTO;
  }

  public static Stream<Arguments> provideResourcesAndDtos() {
    return Stream.of(
        Arguments.of(getGemstone(), getGemstoneDTO()),
        Arguments.of(getLinkingPart(), getLinkingPartDTO()),
        Arguments.of(getPearl(), getPearlDTO()),
        Arguments.of(getPreciousMetal(), getPreciousMetalDTO()));
  }

  public static Stream<Arguments> provideResourcesAndUpdatedDtos() {
    return Stream.of(
        Arguments.of(getGemstone(), getUdpatedGemstoneDTO()),
        Arguments.of(getLinkingPart(), getUpdatedLinkingPartDTO()),
        Arguments.of(getPearl(), getUpdatedPearlDTO()),
        Arguments.of(getPreciousMetal(), getUpdatedPreciousMetalDTO()));
  }

  public static Stream<ResourceDTO> provideResourceDtos() {
    return Stream.of(getGemstoneDTO(), getLinkingPartDTO(), getPearlDTO(), getPreciousMetalDTO());
  }

  public static Stream<Resource> provideResources() {
    return Stream.of(getGemstone(), getLinkingPart(), getPearl(), getPreciousMetal());
  }
}
