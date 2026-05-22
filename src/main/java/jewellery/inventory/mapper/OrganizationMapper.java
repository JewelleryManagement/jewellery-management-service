package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.OrganizationSingleMemberResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.model.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrganizationMapper {
  private UserMapper userMapper;
  private final ScopedRoleMapper scopedRoleMapper;

  public Organization toEntity(OrganizationRequestDto dto) {
    Organization organization = new Organization();
    organization.setName(dto.getName());
    organization.setAddress(dto.getAddress());
    organization.setNote(dto.getNote());
    return organization;
  }

  public OrganizationResponseDto toResponse(Organization organization) {
    if (organization != null) {
      OrganizationResponseDto organizationResponseDto = new OrganizationResponseDto();
      organizationResponseDto.setId(organization.getId());
      organizationResponseDto.setName(organization.getName());
      organizationResponseDto.setAddress(organization.getAddress());
      organizationResponseDto.setNote(organization.getNote());
      return organizationResponseDto;
    }
    return null;
  }

  public OrganizationSingleMemberResponseDto toOrganizationSingleMemberResponseDto(
      UserInOrganization userInOrganization, List<ScopedRole> organizationRoles) {
    OrganizationSingleMemberResponseDto memberResponseDto =
        new OrganizationSingleMemberResponseDto();
    memberResponseDto.setMember(
        toUserInOrganizationResponseDto(userInOrganization, organizationRoles));

    memberResponseDto.setOrganization(toResponse(userInOrganization.getOrganization()));

    return memberResponseDto;
  }

  public UserInOrganizationResponseDto toUserInOrganizationResponseDto(
      UserInOrganization userInOrganization) {
    UserInOrganizationResponseDto userResponseDto = new UserInOrganizationResponseDto();

    userResponseDto.setUser(userMapper.toUserResponse(userInOrganization.getUser()));
    return userResponseDto;
  }

  public UserInOrganizationResponseDto toUserInOrganizationResponseDto(
      UserInOrganization userInOrganization, List<ScopedRole> organizationRoles) {
    UserInOrganizationResponseDto dto = new UserInOrganizationResponseDto();

    dto.setUser(userMapper.toUserResponse(userInOrganization.getUser()));
    dto.setOrganizationRoles(scopedRoleMapper.toResponseList(organizationRoles));

    return dto;
  }
}
