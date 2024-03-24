package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductInOrganizationMapper {
  private final OrganizationMapper organizationMapper;

  public ProductsInOrganizationResponseDto mapToProductResponseDto(
      Organization organization, List<ProductResponseDto> products) {
    ProductsInOrganizationResponseDto productsInOrganizationResponseDto =
        new ProductsInOrganizationResponseDto();

    productsInOrganizationResponseDto.setOrganization(organizationMapper.toResponse(organization));
    productsInOrganizationResponseDto.setProducts(products);
    return productsInOrganizationResponseDto;
  }
}
