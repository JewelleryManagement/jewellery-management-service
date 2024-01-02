package jewellery.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {
  private static final String PRICE_MIN_VALUE_MSG = "Price should not be less than 0.";
  private static final String PRICE_DECIMAL_PLACES_MSG =
      "Quantity should not have more than 2 decimal places.";

  @NotEmpty private List<UUID> authors;
  @NotNull private UUID ownerId;
  @NotEmpty @Valid List<ResourceQuantityRequestDto> resourcesContent;
  private List<UUID> productsContent;
  private String description;
  @NotNull
  @Min(value = 0, message = PRICE_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = PRICE_DECIMAL_PLACES_MSG)
  private BigDecimal salePrice;
  @NotNull private String catalogNumber;
  @NotNull private String productionNumber;
}
