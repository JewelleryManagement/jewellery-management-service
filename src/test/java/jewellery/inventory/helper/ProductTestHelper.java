package jewellery.inventory.helper;

import static jewellery.inventory.utils.BigDecimalUtil.getBigDecimal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import org.jetbrains.annotations.NotNull;

public class ProductTestHelper {

  public static ProductRequestDto getProductRequestDtoForOrganization(
      User author, UUID organizationId, UUID resourceId, BigDecimal quantity) {
    ProductRequestDto productRequestDto = new ProductRequestDto();
    productRequestDto.setOwnerId(organizationId);
    productRequestDto.setProductionNumber("1234");
    productRequestDto.setCatalogNumber("1");
    productRequestDto.setAuthors(List.of(author.getId()));
    productRequestDto.setDescription("This is test product");
    productRequestDto.setAdditionalPrice(BigDecimal.ZERO);
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    resourceQuantityRequestDto.setResourceId(resourceId);
    resourceQuantityRequestDto.setQuantity(quantity);
    productRequestDto.setResourcesContent(List.of(resourceQuantityRequestDto));
    return productRequestDto;
  }

  public static Product getTestProduct(User user, Resource pearl) {
    Product testProduct = new Product();
    testProduct.setAuthors(List.of(user));
    testProduct.setId(UUID.randomUUID());
    testProduct.setProductionNumber("11");
    testProduct.setCatalogNumber("2");
    testProduct.setOwner(user);
    testProduct.setDescription("This is Test Product");
    testProduct.setResourcesContent(List.of(getResourceInProduct(pearl)));
    testProduct.setProductsContent(new ArrayList<>());
    testProduct.setContentOf(null);
    testProduct.setResourcesContent(new ArrayList<>());
    testProduct.setAdditionalPrice(BigDecimal.TEN);
    return testProduct;
  }

  @NotNull
  private static ResourceInProduct getResourceInProduct(Resource pearl) {
    ResourceInProduct resourceInProduct = new ResourceInProduct();
    resourceInProduct.setResource(pearl);
    resourceInProduct.setQuantity(getBigDecimal("5"));
    return resourceInProduct;
  }
}
