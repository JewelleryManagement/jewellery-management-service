package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jewellery.inventory.model.OrganizationPermission;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserPermissionsRequest {
  @NotEmpty private List<OrganizationPermission> organizationPermission;
}
