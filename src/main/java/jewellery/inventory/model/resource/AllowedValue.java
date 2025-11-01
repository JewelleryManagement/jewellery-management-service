package jewellery.inventory.model.resource;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
public class AllowedValue {

    @EmbeddedId
    private AllowedValueId id;

    @Data
    @NoArgsConstructor
    @SuperBuilder
    @Embeddable
    public static class AllowedValueId {
        private String resourceClazz;
        private String fieldName;
        private String value;
    }
}
