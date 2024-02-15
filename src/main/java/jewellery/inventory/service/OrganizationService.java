package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.exception.not_found.OrganizationNotFoundException;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.UserInOrganization;
import jewellery.inventory.repository.OrganizationRepository;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.ResourceInOrganizationRepository;
import jewellery.inventory.repository.SaleRepository;
import jewellery.inventory.repository.UserInOrganizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final ResourceInOrganizationRepository resourceInOrganizationRepository;
  private final ProductRepository productRepository;
  private final UserInOrganizationRepository userInOrganizationRepository;
  private final SaleRepository saleRepository;
  private final OrganizationMapper organizationMapper;

  private List<Organization> getAll() {
    return organizationRepository.findAll();
  }

  private Organization getOrganization(UUID id) {
    return organizationRepository
        .findById(id)
        .orElseThrow(() -> new OrganizationNotFoundException(id));
  }

  public List<OrganizationResponseDto> getAllOrganizationsResponses() {
    return getAll().stream().map(organizationMapper::toResponse).toList();
  }

  public OrganizationResponseDto getOrganizationResponse(UUID id) {
    return organizationMapper.toResponse(getOrganization(id));
  }

  public void create(OrganizationRequestDto organizationRequestDto) {
    Organization organization = new Organization();

    // TODO This repository calls should be refactored. Set like this only temporary - TBD
    List<ResourceInOrganization> resourceInOrganizations =
        resourceInOrganizationRepository.findAll();
    List<Product> productsOwned = productRepository.findAll();
    organization.setProductsOwned(productsOwned);
    List<UserInOrganization> userInOrganizations = userInOrganizationRepository.findAll();
    List<Sale> sales = saleRepository.findAll();

    // TODO This can be moved in separate mapper like everything else. Also I have an idea for
    // another approach - TBD
    organization.setName(organizationRequestDto.getName());
    organization.setAddress(organizationRequestDto.getAddress());
    organization.setNote(organizationRequestDto.getNote());
    organization.setResourceInOrganization(resourceInOrganizations);
    organization.setUserInOrganizations(userInOrganizations);
    organization.setSales(sales);

    organizationRepository.save(organization);

   // return new OrganizationResponseDto(organization);
  }
}
