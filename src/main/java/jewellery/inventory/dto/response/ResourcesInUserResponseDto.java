package jewellery.inventory.dto.response;

import java.util.List;
import jewellery.inventory.dto.ResourceQuantityDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class ResourcesInUserResponseDto {
  private UserResponseDto owner;
  private List<ResourceQuantityDto> resourcesAndQuantities;
}
