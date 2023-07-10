package jewellery.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceDTO {
    private UUID id;
    private String name;
    private String quantityType;
}
