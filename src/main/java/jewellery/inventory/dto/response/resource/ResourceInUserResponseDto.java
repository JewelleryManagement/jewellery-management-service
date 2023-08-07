package jewellery.inventory.dto.response.resource;

import java.util.List;
import jewellery.inventory.dto.response.UserResponseDto;
import lombok.Data;

@Data
public class ResourceInUserResponseDto {
  private UserResponseDto owner;
  List<ResourceResponseDto> resources;
  private double quantity;
}
