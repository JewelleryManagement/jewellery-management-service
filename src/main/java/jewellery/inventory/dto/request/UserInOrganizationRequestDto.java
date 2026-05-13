package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class UserInOrganizationRequestDto {
  @NotNull private UUID userId;
  private List<UUID> organizationRoles = new ArrayList<>();
}
