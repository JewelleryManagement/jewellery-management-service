package jewellery.inventory.dto.response.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "clazz", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PearlResponseDto.class, name = "Pearl"),
        @JsonSubTypes.Type(value = GemstoneResponseDto.class, name = "Gemstone"),
        @JsonSubTypes.Type(value = PreciousMetalResponseDto.class, name = "PreciousMetal"),
        @JsonSubTypes.Type(value = LinkingPartResponseDto.class, name = "LinkingPart")
})
@SuperBuilder
@Data
@NoArgsConstructor
public class ResourceResponseDto {
    private UUID id;
    private String clazz;
    private String quantityType;
}
