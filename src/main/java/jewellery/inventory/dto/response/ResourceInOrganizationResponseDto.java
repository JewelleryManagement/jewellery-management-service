package jewellery.inventory.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class ResourceInOrganizationResponseDto {
    private OrganizationResponseDto owner;
    private ResourceQuantityResponseDto resourcesAndQuantities;
}
