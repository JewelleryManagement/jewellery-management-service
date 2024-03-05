package jewellery.inventory.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourcesInOrganizationResponseDto {
  private OrganizationResponseDto owner;
  private List<ResourceQuantityResponseDto> resourcesAndQuantities;
}
