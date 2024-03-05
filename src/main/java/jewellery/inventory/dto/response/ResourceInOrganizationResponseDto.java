package jewellery.inventory.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInOrganizationResponseDto {
    private OrganizationResponseDto owner;
    private ResourceQuantityResponseDto resourcesAndQuantities;
}
