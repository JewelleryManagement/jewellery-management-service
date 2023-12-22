package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceQuantityRequestDto {

    private UUID id;
    @Positive
    private double quantity;
}
