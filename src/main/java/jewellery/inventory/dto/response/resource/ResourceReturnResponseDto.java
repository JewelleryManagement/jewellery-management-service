package jewellery.inventory.dto.response.resource;

import jewellery.inventory.dto.response.SaleResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
public class ResourceReturnResponseDto {
    private ResourceResponseDto returnedResource;
    private SaleResponseDto saleAfter;
}
