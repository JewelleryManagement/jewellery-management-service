package jewellery.inventory.dto.response;

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
