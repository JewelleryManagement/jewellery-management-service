package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceQuantityRequestDto {
  private UUID resourceId;
  @Positive private BigDecimal quantity;
}
