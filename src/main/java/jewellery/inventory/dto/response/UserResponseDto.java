package jewellery.inventory.dto.response;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.resource.ResourceInUserResponseDto;
import lombok.Data;

@Data
public class UserResponseDto {
  private UUID id;
  private String name;
  private String email;
  List<ResourceInUserResponseDto> resources;
}
