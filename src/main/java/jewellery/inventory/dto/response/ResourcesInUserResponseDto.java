package jewellery.inventory.dto.response;

import java.util.List;
import jewellery.inventory.dto.ResourceQuantityDto;
import lombok.Data;

@Data
public class ResourcesInUserResponseDto {
  private UserResponseDto owner;
  private List<ResourceQuantityDto> resources;
}
