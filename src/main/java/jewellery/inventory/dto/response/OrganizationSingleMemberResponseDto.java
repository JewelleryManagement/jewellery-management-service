package jewellery.inventory.dto.response;

import lombok.Data;

@Data
public class OrganizationSingleMemberResponseDto {
  private UserInOrganizationResponseDto member;
  private OrganizationResponseDto organization;
}
