package jewellery.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {
  @NotEmpty
  private List<UUID> authors;
  @NotNull
  private UUID ownerId;
  @NotEmpty
  @Valid
  List<ResourceQuantityRequestDto> resourcesContent;
  private List<UUID> productsContent;
  private String description;
  @NotNull
  private double salePrice;
  @NotNull
  private String catalogNumber;
  @NotNull
  private String productionNumber;
}
