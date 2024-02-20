package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.model.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class OrganizationMapper {

  private SaleMapper saleMapper;
  private ProductMapper productMapper;
  private UserMapper userMapper;

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
    List<UserInOrganizationResponseDto> list = new ArrayList<>();

    for (int i = 0; i < organization.getUserInOrganizations().size(); i++) {
      UserInOrganizationResponseDto dto = new UserInOrganizationResponseDto();

      dto.setId(organization.getUserInOrganizations().get(i).getId());
      dto.setUser(
          userMapper.toUserResponse(organization.getUserInOrganizations().get(i).getUser()));
      dto.setOrganizationPermission(
          organization.getUserInOrganizations().get(i).getOrganizationPermission());
      list.add(dto);
      dto.setOrganizationId(organization.getId());
    }
    organizationResponseDto.setUserInOrganizations(list);

    return organizationResponseDto;
  }
}
