package jewellery.inventory.dto.response;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.model.OrganizationPermission;
import lombok.Data;

@Data
public class UserInOrganizationResponseDto {

  private UUID id;
  private UserResponseDto user;
  private UUID organizationId;
  private List<OrganizationPermission> organizationPermission;
}
