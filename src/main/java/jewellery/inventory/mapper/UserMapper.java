package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.request.UserRequestDto;
import jewellery.inventory.dto.request.UserUpdateRequestDto;
import jewellery.inventory.dto.response.ExecutorResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserRequestDto toUserRequest(User user);
  UserUpdateRequestDto toUserUpdateRequest(User user);

  UserResponseDto toUserResponse(User user);

  ExecutorResponseDto toExecutorResponse(User user);

  List<UserResponseDto> toUserResponseList(List<User> userList);

  User toUserEntity(UserRequestDto userDto);
  User toUserEntity(UserUpdateRequestDto userDto);
}
