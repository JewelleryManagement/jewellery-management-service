package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.UserRequest;
import jewellery.inventory.dto.UserResponse;
import jewellery.inventory.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserRequest toUserRequest(User user);

  UserResponse toUserResponse(User user);

  List<UserResponse> toUserResponseList(List<User> userList);

  User toUserEntity(UserRequest userDto);
}
