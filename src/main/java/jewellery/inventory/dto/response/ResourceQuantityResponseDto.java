package jewellery.inventory.dto.response;

import java.math.BigDecimal;

import jewellery.inventory.dto.response.resource.ResourceResponseDto;
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
