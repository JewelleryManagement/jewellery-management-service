package jewellery.inventory.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PurchasedResourcesResponseDto {
  private UserResponseDto owner;
  private List<PurchasedResourceQuantityResponseDto> resources;
}
