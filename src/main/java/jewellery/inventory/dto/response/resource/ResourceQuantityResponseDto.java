package jewellery.inventory.dto.response.resource;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceQuantityResponseDto {
    private ResourceResponseDto resource;
    private BigDecimal quantity;
}
