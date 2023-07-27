package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserRequestDto toUserRequest(User user);

  UserResponseDto toUserResponse(User user);

  List<UserResponseDto> toUserResponseList(List<User> userList);

  User toUserEntity(UserRequestDto userDto);
}
