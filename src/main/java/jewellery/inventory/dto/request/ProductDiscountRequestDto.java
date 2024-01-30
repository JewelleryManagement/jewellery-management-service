package jewellery.inventory.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductDiscountRequestDto {
  @NotNull private UUID productId;
  @NotNull
  @DecimalMin(value = "0")
  @DecimalMax(value = "100")
  private BigDecimal discount;}
