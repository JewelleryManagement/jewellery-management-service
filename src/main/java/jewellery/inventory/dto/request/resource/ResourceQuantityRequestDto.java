package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceQuantityRequestDto {

    private static final String MINIMAL_RESOURCE_QUANTITY = "0.01";
    private static final String RESOURCE_QUANTITY_MESSAGE = "Resource quantity should not be less than 0.01";
    private static final String QUANTITY_DECIMAL_PLACES_MSG =
            "Resource quantity should not have more than 2 decimal places.";

    private UUID id;
    @DecimalMin(value = MINIMAL_RESOURCE_QUANTITY, message = RESOURCE_QUANTITY_MESSAGE)
    @Digits(integer = 10, fraction = 2, message = QUANTITY_DECIMAL_PLACES_MSG)
    private double quantity;
}
