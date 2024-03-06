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
import java.util.UUID;

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

  private List<UserInOrganizationResponseDto> toUserInOrganizationResponseDto(
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
      Organization organization, UUID userId) {
    UserInOrganizationResponseDto userResponseDto = new UserInOrganizationResponseDto();

    organization.getUsersInOrganization().stream()
        .filter(userInOrg -> userInOrg.getUser().getId().equals(userId))
        .findFirst()
        .ifPresent(
            userInOrg -> {
              userResponseDto.setUserId(userInOrg.getUser().getId());
              userResponseDto.setOrganizationPermissions(userInOrg.getOrganizationPermission());
            });

    return userResponseDto;
  }

  public OrganizationMembersResponseDto toOrganizationMembersResponseDto(
      Organization organization) {
    OrganizationMembersResponseDto membersResponseDto = new OrganizationMembersResponseDto();
    membersResponseDto.setMembers(toUserInOrganizationResponseDto(organization));
    membersResponseDto.setOrganization(toResponse(organization));
    return membersResponseDto;
  }

  public OrganizationSingleMemberResponseDto toOrganizationSingleMemberResponseDto(
      UUID userId, Organization organization) {
    OrganizationSingleMemberResponseDto memberResponseDto =
        new OrganizationSingleMemberResponseDto();
    memberResponseDto.setMember(toUserInOrganizationResponseDto(organization, userId));
    memberResponseDto.setOrganization(toResponse(organization));
    return memberResponseDto;
  }
}
