package jewellery.inventory.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Data
@RequiredArgsConstructor
public class TransferResourceRequestDto {

    private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 1.";
    private static final String QUANTITY_DECIMAL_PLACES_MSG =
            "Quantity should not have more than 2 decimal places.";

    @NotNull
    private UUID ownerId;
    @NotNull
    private UUID recipientId;
    @NotNull
    private UUID resourceId;

    @Min(value = 0, message = QUANTITY_MIN_VALUE_MSG)
    @Digits(integer = 10, fraction = 2, message = QUANTITY_DECIMAL_PLACES_MSG)
    private double quantity;
}
