package jewellery.inventory.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceInProductRequestDto {

    private UUID id;
    private double quantity;
}
