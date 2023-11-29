package jewellery.inventory.dto.request.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "clazz", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PearlRequestDto.class, name = "Pearl"),
        @JsonSubTypes.Type(value = PreciousStoneRequestDto.class, name = "PreciousStone"),
        @JsonSubTypes.Type(value = MetalRequestDto.class, name = "Metal"),
        @JsonSubTypes.Type(value = ElementRequestDto.class, name = "Element"),
        @JsonSubTypes.Type(value = SemiPreciousStoneRequestDto.class, name = "SemiPreciousStone")
})
@SuperBuilder
@Data
@NoArgsConstructor
public class ResourceRequestDto {
    private String clazz;
    private String quantityType;
    private double pricePerQuantity;
    private String note;
}
