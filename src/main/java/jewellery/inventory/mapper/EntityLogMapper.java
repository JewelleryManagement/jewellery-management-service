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
}
