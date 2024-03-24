package jewellery.inventory.service;

import java.util.UUID;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.mapper.ProductInOrganizationMapper;
import jewellery.inventory.model.Organization;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductInOrganizationService {
  private final OrganizationService organizationService;
  private final ProductService productService;
  private final ProductInOrganizationMapper mapper;

  public ProductsInOrganizationResponseDto getProductsInOrganization(UUID organizationId) {
    Organization organization = organizationService.getOrganization(organizationId);
    organizationService.validateUserInOrganization(organization);
    
    return mapper.mapToProductResponseDto(
        organization, productService.getProductsResponse(organization.getProductsOwned()));
  }
}
