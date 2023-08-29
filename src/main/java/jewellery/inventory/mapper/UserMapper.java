package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.User;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring")
public interface UserMapper {
  UserRequestDto toUserRequest(User user);

  UserResponseDto toUserResponse(User user);

  List<UserResponseDto> toUserResponseList(List<User> userList);

  User toUserEntity(UserRequestDto userDto);
}
