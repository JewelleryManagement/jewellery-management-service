package jewellery.inventory.dto.response;

import lombok.Data;

@Data
public class OrganizationSingleMemberResponseDto {
  private OrganizationResponseDto organization;
  private UserInOrganizationResponseDto member;
}
