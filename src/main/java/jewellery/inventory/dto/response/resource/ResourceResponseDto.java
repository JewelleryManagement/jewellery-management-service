package jewellery.inventory.dto.response.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "clazz",
    visible = true,
    include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
  @JsonSubTypes.Type(value = PearlResponseDto.class, name = "Pearl"),
  @JsonSubTypes.Type(value = DiamondResponseDto.class, name = "Diamond"),
  @JsonSubTypes.Type(value = ColoredStoneResponseDto.class, name = "ColoredStone"),
  @JsonSubTypes.Type(value = ColoredStoneMeleeResponseDto.class, name = "ColoredStoneMelee"),
  @JsonSubTypes.Type(value = MetalResponseDto.class, name = "Metal"),
  @JsonSubTypes.Type(value = ElementResponseDto.class, name = "Element"),
  @JsonSubTypes.Type(value = DiamondMeleeResponseDto.class, name = "DiamondMelee"),
})
@SuperBuilder
@Data
@NoArgsConstructor
public class ResourceResponseDto {
  private UUID id;
  private String clazz;
  private String quantityType;
  private BigDecimal pricePerQuantity;
  private String note;
  private String sku;
}
