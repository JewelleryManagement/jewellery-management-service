package jewellery.inventory.dto.request;

import java.util.List;
import jewellery.inventory.model.OrganizationPermission;
import lombok.Data;

@Data
public class UpdateUserPermissionsRequest {
  private List<OrganizationPermission> organizationPermission;
}
