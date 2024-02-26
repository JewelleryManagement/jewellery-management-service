package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
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

  public List<UserInOrganizationResponseDto> toUserInOrganizationResponseDtoResponse(
      Organization organization) {
    List<UserInOrganizationResponseDto> list = new ArrayList<>();
    for (int i = 0; i < organization.getUsersInOrganization().size(); i++) {
      UserInOrganizationResponseDto user = new UserInOrganizationResponseDto();
      user.setUserId(organization.getUsersInOrganization().get(i).getUser().getId());
      user.setOrganizationPermissions(
          organization.getUsersInOrganization().get(i).getOrganizationPermission());
      list.add(user);
    }
    return list;
  }
}
