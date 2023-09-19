package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.ResourceQuantityDto;
import jewellery.inventory.dto.UserQuantityDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
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

  public ResourceOwnedByUsersResponseDto toResourcesOwnedByUsersResponseDto(Resource resource) {
    List<UserQuantityDto> userQuantityDtos =
        resource.getUserAffiliations().stream()
            .map(
                resourceInUser ->
                    UserQuantityDto.builder()
                        .owner(userMapper.toUserResponse(resourceInUser.getOwner()))
                        .quantity(resourceInUser.getQuantity())
                        .build())
            .toList();
    return ResourceOwnedByUsersResponseDto.builder()
        .usersAndQuantities(userQuantityDtos)
        .resource(resourceMapper.toResourceResponse(resource))
        .build();
  }
}
