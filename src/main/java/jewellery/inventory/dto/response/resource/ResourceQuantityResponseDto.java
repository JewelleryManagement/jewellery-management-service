package jewellery.inventory.dto.response.resource;

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
    private double quantity;
}
