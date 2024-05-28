package jewellery.inventory.dto.response;

import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class ResourceOwnedByOrganizationsResponseDto {
    private ResourceResponseDto resource;
    private List<OrganizationQuantityResponseDto> organizationsAndQuantities;
}
