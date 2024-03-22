package jewellery.inventory.dto.response;

import lombok.Data;
import java.util.List;
@Data
public class OrganizationMembersResponseDto {
    OrganizationResponseDto organization;
    List<UserInOrganizationResponseDto> members;
}
