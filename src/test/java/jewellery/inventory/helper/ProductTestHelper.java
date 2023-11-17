package jewellery.inventory.helper;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ResourcesInUserResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductTestHelper {

  public static ResourceInUser getResourceInUser(User user, Resource pearl) {
    ResourceInUser resourceInUser = new ResourceInUser();
    resourceInUser.setId(UUID.randomUUID());
    resourceInUser.setOwner(user);
    resourceInUser.setResource(pearl);
    resourceInUser.setQuantity(20);
    return resourceInUser;
  }

  public static ProductRequestDto getProductRequestDto(
      User user, ResourceQuantityRequestDto resourceQuantityRequestDto) {
    ProductRequestDto productRequestDto = new ProductRequestDto();
    productRequestDto.setProductionNumber("1234");
    productRequestDto.setCatalogNumber("1");
    productRequestDto.setAuthors(List.of(user.getId()));
    productRequestDto.setOwnerId(user.getId());
    productRequestDto.setDescription("This is test product");
    productRequestDto.setResourcesContent(List.of(resourceQuantityRequestDto));
    productRequestDto.setSalePrice(10000);
    return productRequestDto;
  }

  @NotNull
  public static ProductRequestDto getProductRequestDto(
      ResourcesInUserResponseDto resourcesInUser, User user) {

    List<ResourceQuantityRequestDto> listOfResourcesInProduct =
        getResourcesInProductRequestDto(resourcesInUser);

    ProductRequestDto productRequestDto = new ProductRequestDto();
    productRequestDto.setCatalogNumber("1111");
    productRequestDto.setProductionNumber("1234");
    productRequestDto.setDescription("Test");
    productRequestDto.setOwnerId(user.getId());
    productRequestDto.setAuthors(List.of(user.getId()));
    productRequestDto.setSalePrice(50);
    productRequestDto.setResourcesContent(listOfResourcesInProduct);

    return productRequestDto;
  }

  public static ResourceQuantityRequestDto getResourceQuantityRequestDto(Resource pearl) {
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    resourceQuantityRequestDto.setId(pearl.getId());
    resourceQuantityRequestDto.setQuantity(5);
    return resourceQuantityRequestDto;
  }

  public static Product getTestProduct(User user, Resource pearl) {
    Product testProduct = new Product();
    testProduct.setAuthors(List.of(user));
    testProduct.setId(UUID.randomUUID());
    testProduct.setProductionNumber("11");
    testProduct.setCatalogNumber("2");
    testProduct.setOwner(user);
    testProduct.setDescription("This is Test Product");
    testProduct.setSalePrice(1000);
    testProduct.setResourcesContent(List.of(getResourceInProduct(pearl)));
    testProduct.setProductsContent(null);
    testProduct.setContentOf(null);
    return testProduct;
  }
  public static ProductResponseDto getРeturnedProductResponseDto(Product product, UserResponseDto owner){
    ProductResponseDto productResponseDto=new ProductResponseDto();
    productResponseDto.setId(product.getId());
    productResponseDto.setAuthors(null);
    productResponseDto.setOwner(owner);
    productResponseDto.setResourcesContent(null);
    productResponseDto.setProductsContent(null);
    productResponseDto.setContentOf(null);
    productResponseDto.setDescription("description");
    productResponseDto.setSalePrice(1000);
    productResponseDto.setPartOfSale(null);
    productResponseDto.setCatalogNumber("Catalog Number");
    productResponseDto.setProductionNumber("Production Number");
    productResponseDto.setDiscount(0);
    return productResponseDto;
  }

  @NotNull
  private static ResourceInProduct getResourceInProduct(Resource pearl) {
    ResourceInProduct resourceInProduct = new ResourceInProduct();
    resourceInProduct.setResource(pearl);
    resourceInProduct.setQuantity(5);
    return resourceInProduct;
  }

  @NotNull
  private static List<ResourceQuantityRequestDto> getResourcesInProductRequestDto(
      ResourcesInUserResponseDto resourcesInUser) {
    ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
    List<ResourceQuantityRequestDto> listOfResourcesInProduct = new ArrayList<>();
    resourcesInUser
        .getResourcesAndQuantities()
        .forEach(
            r -> {
              resourceQuantityRequestDto.setId(r.getResource().getId());
              resourceQuantityRequestDto.setQuantity(5);
              listOfResourcesInProduct.add(resourceQuantityRequestDto);
            });
    return listOfResourcesInProduct;
  }
}
