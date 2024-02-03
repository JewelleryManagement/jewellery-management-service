package jewellery.inventory.dto.response;

import java.util.List;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.UserInOrganization;
import lombok.Data;

@Data
public class OrganizationResponseDto {
  private String name;
  private String address;
  private String note;
  private List<ResourceInOrganization> resourceInOrganization;
  private List<Product> productsOwned;
  private List<UserInOrganization> userInOrganizations;
  private List<Sale> sales;

  public OrganizationResponseDto(Organization organization) {
    this.name = organization.getName();
    this.address = organization.getAddress();
    this.note = organization.getNote();
    this.resourceInOrganization = organization.getResourceInOrganization();
    this.productsOwned = organization.getProductsOwned();
    this.userInOrganizations = organization.getUserInOrganizations();
    this.sales = organization.getSales();
  }
}
