package jewellery.inventory.mapper;

import jewellery.inventory.dto.EntityLogDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import jewellery.inventory.dto.response.resource.LinkingPartResponseDto;
import jewellery.inventory.dto.response.resource.PearlResponseDto;
import jewellery.inventory.dto.response.resource.PreciousMetalResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EntityLogMapper {
  EntityLogMapper INSTANCE = Mappers.getMapper(EntityLogMapper.class);

  EntityLogDto toEntityLogDto(UserResponseDto user);

  EntityLogDto toEntityLogDto(ResourcesInUserResponseDto resource);

  EntityLogDto toEntityLogDto(PearlResponseDto pearl);

  EntityLogDto toEntityLogDto(GemstoneResponseDto gemstone);

  EntityLogDto toEntityLogDto(LinkingPartResponseDto linkingPart);

  EntityLogDto toEntityLogDto(PreciousMetalResponseDto preciousMetal);

  default EntityLogDto mapEntityToDto(Object entity) {
    return switch (entity.getClass().getSimpleName()) {
      case "UserResponseDto" -> toEntityLogDto((UserResponseDto) entity);
      case "ResourcesInUserResponseDto" -> toEntityLogDto((ResourcesInUserResponseDto) entity);
      case "PearlResponseDto" -> toEntityLogDto((PearlResponseDto) entity);
      case "GemstoneResponseDto" -> toEntityLogDto((GemstoneResponseDto) entity);
      case "LinkingPartResponseDto" -> toEntityLogDto((LinkingPartResponseDto) entity);
      case "PreciousMetalResponseDto" -> toEntityLogDto((PreciousMetalResponseDto) entity);
      default -> throw new IllegalArgumentException(
          "Unsupported entity type: " + entity.getClass());
    };
  }
}
