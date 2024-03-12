package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationMembersResponseDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.OrganizationSingleMemberResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.model.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class OrganizationMapper {

  public Organization toEntity(OrganizationRequestDto dto) {
    Organization organization = new Organization();
    organization.setName(dto.getName());
    organization.setAddress(dto.getAddress());
    organization.setNote(dto.getNote());
    return organization;
  }

  public OrganizationResponseDto toResponse(Organization organization) {
    OrganizationResponseDto organizationResponseDto = new OrganizationResponseDto();
    organizationResponseDto.setId(organization.getId());
    organizationResponseDto.setName(organization.getName());
    organizationResponseDto.setAddress(organization.getAddress());
    organizationResponseDto.setNote(organization.getNote());
    return organizationResponseDto;
  }

  public OrganizationMembersResponseDto toOrganizationMembersResponseDto(
      Organization organization) {
    OrganizationMembersResponseDto membersResponseDto = new OrganizationMembersResponseDto();
    membersResponseDto.setMembers(toListUserInOrganizationResponseDto(organization));
    membersResponseDto.setOrganization(toResponse(organization));
    return membersResponseDto;
  }

  public OrganizationSingleMemberResponseDto toOrganizationSingleMemberResponseDto(
      UserInOrganization userInOrganization) {
    OrganizationSingleMemberResponseDto memberResponseDto =
        new OrganizationSingleMemberResponseDto();
    memberResponseDto.setMember(toUserInOrganizationResponseDto(userInOrganization));
    memberResponseDto.setOrganization(toResponse(userInOrganization.getOrganization()));
    return memberResponseDto;
  }

  private List<UserInOrganizationResponseDto> toListUserInOrganizationResponseDto(
      Organization organization) {
    List<UserInOrganizationResponseDto> userResponseDtoList = new ArrayList<>();

    organization
        .getUsersInOrganization()
        .forEach(
            userInOrg -> {
              UserInOrganizationResponseDto userResponseDto = new UserInOrganizationResponseDto();
              userResponseDto.setUserId(userInOrg.getUser().getId());
              userResponseDto.setOrganizationPermissions(userInOrg.getOrganizationPermission());
              userResponseDtoList.add(userResponseDto);
            });

    return userResponseDtoList;
  }

  private UserInOrganizationResponseDto toUserInOrganizationResponseDto(
      UserInOrganization userInOrganization) {
    UserInOrganizationResponseDto userResponseDto = new UserInOrganizationResponseDto();

    userResponseDto.setUserId(userInOrganization.getId());
    userResponseDto.setOrganizationPermissions(userInOrganization.getOrganizationPermission());

    return userResponseDto;
  }
}
