package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.UserUpdateRequestDto;
import jewellery.inventory.dto.response.DetailedUserResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.UserWithRolesResponseDto;
import jewellery.inventory.model.ScopedRole;
import jewellery.inventory.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ScopedRoleMapper.class)
public interface UserMapper {

  UserRequestDto toUserRequest(User user);

  UserUpdateRequestDto toUserUpdateRequest(User user);

  DetailedUserResponseDto toDetailedUserResponse(User user);

  UserResponseDto toUserResponse(User user);

  List<DetailedUserResponseDto> toDetailedUserResponseList(List<User> userList);

  User toUserEntity(UserRequestDto userDto);

  User toUserEntity(UserUpdateRequestDto userDto);

  @Mapping(target = "user", source = "user")
  @Mapping(target = "roles", source = "roles")
  UserWithRolesResponseDto toUserWithRolesResponseDto(User user, List<ScopedRole> roles);
}
