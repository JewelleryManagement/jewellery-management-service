package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.ResourceQuantityDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourcesInUserMapper {
  private final UserMapper userMapper;

  private final ResourceMapper resourceMapper;

  public ResourcesInUserResponseDto toResourcesInUserResponseDto(User user) {
    List<ResourceQuantityDto> resourcesWithQuantities =
        user.getResourcesOwned().stream()
            .map(
                resourceInUser ->
                    ResourceQuantityDto.builder()
                        .resource(resourceMapper.toResourceResponse(resourceInUser.getResource()))
                        .quantity(resourceInUser.getQuantity())
                        .build())
            .toList();

    ResourcesInUserResponseDto responseDto = new ResourcesInUserResponseDto();

    responseDto.setOwner(userMapper.toUserResponse(user));
    responseDto.setResourcesAndQuantities(resourcesWithQuantities);
    return responseDto;
  }
}
