package jewellery.inventory.dto.response.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "clazz", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PearlResponseDto.class, name = "Pearl"),
        @JsonSubTypes.Type(value = PreciousStoneResponseDto.class, name = "PreciousStone"),
        @JsonSubTypes.Type(value = MetalResponseDto.class, name = "Metal"),
        @JsonSubTypes.Type(value = ElementResponseDto.class, name = "Element"),
        @JsonSubTypes.Type(value = SemiPreciousStoneResponseDto.class, name = "SemiPreciousStone")
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
}
