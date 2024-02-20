package jewellery.inventory.dto.response;

import jakarta.persistence.*;
import jewellery.inventory.model.OrganizationPermission;
import jewellery.inventory.model.User;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserInOrganizationResponseDto {

  private UUID id;
  private UserResponseDto user;
  private UUID organizationId;
  private List<OrganizationPermission> organizationPermission;
}
