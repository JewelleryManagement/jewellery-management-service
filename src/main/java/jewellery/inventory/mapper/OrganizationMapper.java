package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.model.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

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
}
