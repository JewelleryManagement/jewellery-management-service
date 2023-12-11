package jewellery.inventory.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class ResourcePurchaseResponseDto extends ResourcesInUserResponseDto {

    private double dealPrice;
}
