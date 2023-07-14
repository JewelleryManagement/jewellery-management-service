package jewellery.inventory.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "clazz", visible = true, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PearlDTO.class, name = "Pearl"),
        @JsonSubTypes.Type(value = GemstoneDTO.class, name = "Gemstone"),
        @JsonSubTypes.Type(value = PreciousMetalDTO.class, name = "PreciousMetal"),
        @JsonSubTypes.Type(value = LinkingPartDTO.class, name = "LinkingPart")
})
@SuperBuilder
@Data
@NoArgsConstructor
public class ResourceDTO {
    private UUID id;
    private String clazz;
    private String quantityType;
}
