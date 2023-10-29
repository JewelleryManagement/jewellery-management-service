package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
  @NotEmpty private List<ProductPriceDiscountRequestDto> products;
}
