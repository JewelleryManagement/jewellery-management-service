package jewellery.inventory.dto.request.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceQuantityRequestDto {

    private UUID id;
    private double quantity;
}
