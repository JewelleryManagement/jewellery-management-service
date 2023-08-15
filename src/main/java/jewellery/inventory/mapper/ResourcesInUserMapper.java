package jewellery.inventory.mapper;

import java.util.List;
import java.util.stream.Collectors;
import jewellery.inventory.dto.ResourceQuantityDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ResourcesInUserMapper {

  @Mapping(
      source = "resourceInUser",
      target = "resources",
      qualifiedByName = "toResourceQuantityList")
  ResourcesInUserResponseDto toResourceInUserResponseDto(ResourceInUser resourceInUser);

  @Mapping(source = "user", target = "owner", qualifiedByName = "toUserResponse")
  @Mapping(source = "resourcesOwned", target = "resources")
  ResourcesInUserResponseDto toResourcesInUserResponseDto(User user);

  @Named("toResourceQuantityList")
  default List<ResourceQuantityDto> toResourceQuantityList(ResourceInUser resourceInUser) {
    return resourceInUser.getOwner().getResourcesOwned().stream()
        .map(this::toResourceQuantityDto)
        .collect(Collectors.toList());
  }

  @Mapping(source = "resource", target = "resource")
  ResourceQuantityDto toResourceQuantityDto(ResourceInUser resourceInUser);

  @Named("toUserResponse")
  UserResponseDto toUserResponse(User user);
}
