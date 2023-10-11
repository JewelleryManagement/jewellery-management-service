package jewellery.inventory.helper;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ProductTestHelper {

    public static ResourceInUser createResourceInUser(User user, Resource pearl) {
        ResourceInUser resourceInUser = new ResourceInUser();
        resourceInUser.setId(UUID.randomUUID());
        resourceInUser.setOwner(user);
        resourceInUser.setResource(pearl);
        resourceInUser.setQuantity(20);
        return resourceInUser;
    }

    public static ProductRequestDto createProductRequestDto(User user, ResourceQuantityRequestDto resourceQuantityRequestDto) {
        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setOwnerId(user.getId());
        productRequestDto.setName("TestProductDto");
        productRequestDto.setAuthors(List.of("Ivan", "Petar"));
        productRequestDto.setDescription("This is test product");
        productRequestDto.setResourcesContent(List.of(resourceQuantityRequestDto));
        productRequestDto.setSalePrice(10000);
        return productRequestDto;
    }

    public static ResourceQuantityRequestDto createResourceQuantityRequestDto(Resource pearl) {
        ResourceQuantityRequestDto resourceQuantityRequestDto = new ResourceQuantityRequestDto();
        resourceQuantityRequestDto.setId(pearl.getId());
        resourceQuantityRequestDto.setQuantity(5);
        return resourceQuantityRequestDto;
    }

    public static Product createTestProduct(User user, Resource pearl) {
        Product testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setOwner(user);
        testProduct.setName("TestProduct");
        testProduct.setAuthors(List.of("Gosho"));
        testProduct.setSold(false);
        testProduct.setDescription("This is Test Product");
        testProduct.setSalePrice(1000);
        testProduct.setResourcesContent(List.of(createResourceInProduct(pearl)));
        testProduct.setProductsContent(null);
        testProduct.setContent(null);
        return testProduct;
    }

    @NotNull
    private static ResourceInProduct createResourceInProduct(Resource pearl) {
        ResourceInProduct resourceInProduct = new ResourceInProduct();
        resourceInProduct.setResource(pearl);
        resourceInProduct.setQuantity(5);
        return resourceInProduct;
    }
}
