package jewellery.inventory.dto.response;

import jewellery.inventory.model.OrganizationPermission;
import lombok.Data;
import java.util.List;

@Data
public class UserInOrganizationResponseDto {
  private UserResponseDto user;
  private List<OrganizationPermission> organizationPermissions;
}
