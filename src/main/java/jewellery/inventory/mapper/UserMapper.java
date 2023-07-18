package jewellery.inventory.mapper;

import jewellery.inventory.dto.UserDto;
import jewellery.inventory.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserDto toDto(User user);

  User toEntity(UserDto userDto);
}
