package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.UserQuantityDto;
import jewellery.inventory.dto.response.ResourceOwnedByUsersResponseDto;
import jewellery.inventory.dto.response.ResourcePurchaseResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourcesInUserMapper {
  private final UserMapper userMapper;

  private final ResourceMapper resourceMapper;

  public ResourcesInUserResponseDto toResourcePurchaseResponse(ResourceInUser resourceInUser) {
    ResourcePurchaseResponseDto responseDto = new ResourcePurchaseResponseDto();
    responseDto.setOwner(userMapper.toUserResponse(resourceInUser.getOwner()));
    responseDto.setResourcesAndQuantities(getSingleResourceQuantityResponse(resourceInUser));
    responseDto.setDealPrice(resourceInUser.getDealPrice());
    return responseDto;
  }

  public ResourcesInUserResponseDto toResourcesInUserResponseDto(ResourceInUser resourceInUser) {
      ResourcesInUserResponseDto responseDto = new ResourcesInUserResponseDto();
      responseDto.setOwner(userMapper.toUserResponse(resourceInUser.getOwner()));
      responseDto.setResourcesAndQuantities(getSingleResourceQuantityResponse(resourceInUser));
      return responseDto;
  }

  public ResourcesInUserResponseDto toResourcesInUserResponseDto(User user) {
    List<ResourceQuantityResponseDto> resourcesWithQuantities =
        user.getResourcesOwned().stream()
            .map(
                resourceInUser ->
                    ResourceQuantityResponseDto.builder()
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

  private List<ResourceQuantityResponseDto> getSingleResourceQuantityResponse(
      ResourceInUser resourceInUser) {
    return List.of(
        ResourceQuantityResponseDto.builder()
            .resource(resourceMapper.toResourceResponse(resourceInUser.getResource()))
            .quantity(resourceInUser.getQuantity())
            .build());
  }
}
