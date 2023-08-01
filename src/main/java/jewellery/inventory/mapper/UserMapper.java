package jewellery.inventory.mapper;


import java.util.ArrayList;
import java.util.List;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceInUserResponseDto;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.ResourceInUser;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserRequestDto toUserRequest(User user);

  UserResponseDto toUserResponse(User user);

  List<UserResponseDto> toUserResponseList(List<User> userList);

  User toUserEntity(UserRequestDto userDto);

  @Mapping(source = "resource.id", target = "resourceId")
  @Mapping(source = "resource.clazz", target = "resourceClazz")
  @Mapping(source = "owner.name", target = "ownerName")
  ResourceInUserResponseDto toResourceInUserResponseDto(ResourceInUser resourceInUser);

  @AfterMapping
  default void mapResources(User user, @MappingTarget UserResponseDto userResponseDto) {
    if (user.getResourcesOwned() != null) {
      List<ResourceInUserResponseDto> resourcesDtoList = new ArrayList<>();
      for (ResourceInUser resourceInUser : user.getResourcesOwned()) {
        resourcesDtoList.add(toResourceInUserResponseDto(resourceInUser));
      }
      userResponseDto.setResources(resourcesDtoList);
    }
  }
}
