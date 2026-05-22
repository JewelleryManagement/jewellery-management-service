package jewellery.inventory.dto.request;

import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public class UpdateUserInOrganizationRequest {
  private Set<UUID> organizationRoles;
}
