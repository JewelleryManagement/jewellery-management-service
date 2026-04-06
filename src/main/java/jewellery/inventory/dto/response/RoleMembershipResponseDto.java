package jewellery.inventory.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class RoleMembershipResponseDto {
  private UUID id;
  private UserResponseDto userResponseDto;
  private OrganizationResponseDto organizationResponseDto;
  private ScopedRoleResponseDto scopedRoleResponseDto;
}
