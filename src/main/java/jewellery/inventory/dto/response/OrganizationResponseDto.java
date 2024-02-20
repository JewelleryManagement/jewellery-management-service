package jewellery.inventory.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {
  private UUID id;
  private String name;
  private String address;
  private String note;
  private List<ResourceInOrganizationResponseDto> resourceInOrganization;
  private List<ProductResponseDto> productsOwned;
  private List<UserInOrganizationResponseDto> userInOrganizations;
  private List<SaleResponseDto> sales;
}
