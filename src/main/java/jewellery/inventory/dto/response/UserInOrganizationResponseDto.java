package jewellery.inventory.dto.response;

import jewellery.inventory.model.OrganizationPermission;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class UserInOrganizationResponseDto {
  private UUID userId;
  private List<OrganizationPermission> organizationPermissions;
}
