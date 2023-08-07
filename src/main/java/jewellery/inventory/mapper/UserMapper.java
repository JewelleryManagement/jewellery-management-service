package jewellery.inventory.mapper;

import java.util.Collections;
import java.util.List;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceInUserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.ResourceInUser;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserRequestDto toUserRequest(User user);

  UserResponseDto toUserResponse(User user);

  List<UserResponseDto> toUserResponseList(List<User> userList);

  User toUserEntity(UserRequestDto userDto);

  @Mapping(source = "owner", target = "owner", qualifiedByName = "toUserResponse")
  @Mapping(target = "resources", ignore = true)
  ResourceInUserResponseDto toResourceInUserResponseDto(ResourceInUser resourceInUser);

  @AfterMapping
  default void enrichResourceInUserResponseDto(
      ResourceInUser resourceInUser,
      @MappingTarget ResourceInUserResponseDto resourceInUserResponseDto) {
    if (resourceInUser.getResource() != null) {
      ResourceResponseDto resourceResponseDto =
          ResourceMapper.toResourceResponse(resourceInUser.getResource());
      resourceInUserResponseDto.setResources(Collections.singletonList(resourceResponseDto));
    }
  }

  @Named("toUserResponse")
  default UserResponseDto map(User user) {
    return user != null ? UserMapper.INSTANCE.toUserResponse(user) : null;
  }
}
