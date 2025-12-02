package jewellery.inventory.dto.request.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "clazz",
    visible = true,
    include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
  @JsonSubTypes.Type(value = PearlRequestDto.class, name = "Pearl"),
  @JsonSubTypes.Type(value = DiamondRequestDto.class, name = "Diamond"),
  @JsonSubTypes.Type(value = ColoredStoneRequestDto.class, name = "ColoredStone"),
  @JsonSubTypes.Type(value = ColoredStoneMeleeRequestDto.class, name = "ColoredStoneMelee"),
  @JsonSubTypes.Type(value = MetalRequestDto.class, name = "Metal"),
  @JsonSubTypes.Type(value = ElementRequestDto.class, name = "Element"),
  @JsonSubTypes.Type(value = DiamondMeleeRequestDto.class, name = "DiamondMelee"),
  @JsonSubTypes.Type(value = SemiPreciousStoneRequestDto.class, name = "SemiPreciousStone"),
})
@SuperBuilder
@Data
@NoArgsConstructor
public class ResourceRequestDto {
  private String clazz;
  private String quantityType;
  @Positive private BigDecimal pricePerQuantity;
  private String note;
  private String sku;
}
