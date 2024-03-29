package jewellery.inventory.dto.response;

import java.math.BigDecimal;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

  private UUID id;
  private List<UserResponseDto> authors;
  private UserResponseDto owner;
  private List<ResourceQuantityResponseDto> resourcesContent;
  private List<ProductResponseDto> productsContent;
  private UUID contentOf;
  private String description;
  private BigDecimal salePrice;
  private BigDecimal additionalPrice;
  private UUID partOfSale;
  private String catalogNumber;
  private String productionNumber;
  private BigDecimal discount;
}
