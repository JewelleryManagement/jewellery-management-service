package jewellery.inventory.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationQuantityResponseDto {
    private OrganizationResponseDto owner;
    private BigDecimal quantity;
}
