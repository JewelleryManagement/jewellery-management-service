package jewellery.inventory.dto.response;

import java.util.List;
import jewellery.inventory.dto.UserQuantityDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class ResourceOwnedByUsersResponseDto {
  ResourceResponseDto resource;
  private List<UserQuantityDto> usersAndQuantities;
}
