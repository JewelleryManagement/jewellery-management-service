package jewellery.inventory.dto.response.resource;

import java.util.List;
import jewellery.inventory.dto.response.UserResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PurchasedResourceResponseDto {
  private UserResponseDto owner;
  private List<PurchasedResourceQuantityResponseDto> resources;
}
