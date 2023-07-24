package jewellery.inventory.dto.request.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "clazz", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PearlRequestDto.class, name = "Pearl"),
        @JsonSubTypes.Type(value = GemstoneRequestDto.class, name = "Gemstone"),
        @JsonSubTypes.Type(value = PreciousMetalRequestDto.class, name = "PreciousMetal"),
        @JsonSubTypes.Type(value = LinkingPartRequestDto.class, name = "LinkingPart")
})
@SuperBuilder
@Data
@NoArgsConstructor
public class ResourceRequestDto {
    private String clazz;
    private String quantityType;
}
