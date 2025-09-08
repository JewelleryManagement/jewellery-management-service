package jewellery.inventory.dto.response.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllowedValueResponseDto {
    private String resourceClazz;
    private String fieldName;
    private String value;
} 