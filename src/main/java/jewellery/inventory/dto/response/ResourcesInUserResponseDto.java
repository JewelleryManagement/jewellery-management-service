package jewellery.inventory.dto.response;

import java.util.List;

import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
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
  private List<ResourceQuantityResponseDto> resourcesAndQuantities;
}
