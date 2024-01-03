package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceQuantityRequestDto {
  private UUID id;

  @Positive
  private BigDecimal quantity;
}
