package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@RequiredArgsConstructor
public class TransferResourceRequestDto {
  @NotNull private UUID previousOwnerId;
  @NotNull private UUID newOwnerId;
  @NotNull private UUID transferredResourceId;
  @Positive private BigDecimal quantity;
}
