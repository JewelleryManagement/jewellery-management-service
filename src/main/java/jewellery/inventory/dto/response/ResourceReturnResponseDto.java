package jewellery.inventory.dto.response;

import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
public class ResourceReturnResponseDto {
  private ResourceResponseDto returnedResource;
  private SaleResponseDto saleAfter;
}
