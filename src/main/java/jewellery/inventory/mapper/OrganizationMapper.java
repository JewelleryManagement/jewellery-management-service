package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrganizationMapper {

  public OrganizationResponseDto toResponse(Organization organization) {
    OrganizationResponseDto organizationResponseDto = new OrganizationResponseDto();

    organizationResponseDto.setName(organization.getName());
    organizationResponseDto.setAddress(organization.getAddress());
    organizationResponseDto.setNote(organization.getNote());
    List<ResourceInOrganization> list = organization.getResourceInOrganization();
    if (!list.isEmpty()) {
      organizationResponseDto.setResourceInOrganization(list);
    }
    List<Product> list1 = organization.getProductsOwned();
    if (!list1.isEmpty()) {
      organizationResponseDto.setProductsOwned(list1);
    }
    List<UserInOrganization> list2 = organization.getUserInOrganizations();
    if (!list2.isEmpty()) {
      organizationResponseDto.setUserInOrganizations(list2);
    }
    List<Sale> list3 = organization.getSales();
    if (!list3.isEmpty()) {
      organizationResponseDto.setSales(list3);
    }

    return organizationResponseDto;
  }
}
