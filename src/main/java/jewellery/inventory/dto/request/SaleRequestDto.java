package jewellery.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SaleRequestDto {
  @NotNull private UUID sellerId;
  @NotNull private UUID buyerId;

  @Valid @NotEmpty private List<ProductPriceDiscountRequestDto> products;

  @PastOrPresent(message = "Date must be in the past or present")
  private Instant date;
}
