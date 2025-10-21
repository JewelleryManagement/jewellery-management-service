package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllowedValueRequestDto {
  @NotEmpty private String resourceClazz;
  @NotEmpty private String fieldName;
  @NotEmpty private String value;
}
