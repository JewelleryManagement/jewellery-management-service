package jewellery.inventory.dto.response.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ResourceInProductResponseDto {

    private ResourceResponseDto resource;
    private double quantity;
}
