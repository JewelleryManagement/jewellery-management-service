package jewellery.inventory.dto.response;

import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResourceResponseDto {

    UserResponseDto previousOwner;
    UserResponseDto newOwner;
    ResourceQuantityResponseDto transferredResource;
}
