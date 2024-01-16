package jewellery.inventory.dto.response;

import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class ResourcePurchaseResponseDto extends ResourcesInUserResponseDto {

    private BigDecimal dealPrice;
}
