package jewellery.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProductsInOrganizationResponseDto {
    private OrganizationResponseDto organization;
    private List<ProductResponseDto> products;
}
