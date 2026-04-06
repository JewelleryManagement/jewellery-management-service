package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.RoleMembershipResponseDto;
import jewellery.inventory.model.RoleMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, OrganizationMapper.class, ScopedRoleMapper.class})
public interface RoleMembershipMapper {

  @Mapping(target = "userResponseDto", source = "user")
  @Mapping(target = "organizationResponseDto", source = "organization")
  @Mapping(target = "scopedRoleResponseDto", source = "role")
  RoleMembershipResponseDto toResponse(RoleMembership entity);
}
