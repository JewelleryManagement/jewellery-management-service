package jewellery.inventory.dto.request.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllowedValueRequestDto {
    private String resourceClazz;
    private String fieldName;
    private String value;
} 