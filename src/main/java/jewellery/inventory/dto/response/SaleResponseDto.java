package jewellery.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class SaleResponseDto {
  private UUID id;
  private UserResponseDto seller;
  private UserResponseDto buyer;
  private List<ProductResponseDto> products;
  private List<PurchasedResourceQuantityResponseDto> resources;

  @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
  private LocalDateTime date;

  private BigDecimal totalPrice;
  private BigDecimal totalDiscountedPrice;
  private BigDecimal totalDiscount;
}
