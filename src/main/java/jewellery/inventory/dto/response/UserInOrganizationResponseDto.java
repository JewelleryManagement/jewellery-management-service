package jewellery.inventory.dto.response;

import jewellery.inventory.model.OrganizationPermission;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.UUID;
@AllArgsConstructor
public class UserInOrganizationResponseDto {
private UUID userId;
private List<OrganizationPermission> organizationPermissions;
}
