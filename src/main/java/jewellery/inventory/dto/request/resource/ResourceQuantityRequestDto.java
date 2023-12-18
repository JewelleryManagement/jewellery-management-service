package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceQuantityRequestDto {

    private static final String MINIMAL_RESOURCE_QUANTITY = "0.01";
    private static final String RESOURCE_QUANTITY_MESSAGE = "Resource quantity should not be less than 0.01";

    private UUID id;
    @DecimalMin(value = MINIMAL_RESOURCE_QUANTITY, message = RESOURCE_QUANTITY_MESSAGE)
    private double quantity;
}
