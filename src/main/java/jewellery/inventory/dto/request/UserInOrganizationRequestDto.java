package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jewellery.inventory.model.OrganizationPermission;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class UserInOrganizationRequestDto {
    @NotNull
    private UUID userId;
    @NotEmpty
    private List<OrganizationPermission> organizationPermission;
}
