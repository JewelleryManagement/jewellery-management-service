package jewellery.inventory.controller;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInProductRequestDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Pearl;
import jewellery.inventory.model.resource.ResourceInProduct;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ProductTestHelper {

    @NotNull
    public static ProductRequestDto getProductRequest(User user, ResourceInProductRequestDto resourceInProductRequestDto) {

        ProductRequestDto productRequestDto = new ProductRequestDto();
        productRequestDto.setOwnerId(user.getId());
        productRequestDto.setName("TestProductDto");
        productRequestDto.setAuthors(List.of("Ivan", "Petar"));
        productRequestDto.setDescription("This is test product");
        productRequestDto.setResourcesContent(List.of(resourceInProductRequestDto));
        productRequestDto.setSalePrice(10000);

        return productRequestDto;
    }

    @NotNull
    public static Pearl getPearl() {
        Pearl pearl = new Pearl();
        pearl.setId(UUID.randomUUID());
        return pearl;
    }

    @NotNull
    public static ResourceInUser getResourceInUser(User user, Pearl pearl) {
        ResourceInUser resourceInUser = new ResourceInUser();
        resourceInUser.setId(UUID.randomUUID());
        resourceInUser.setOwner(user);
        resourceInUser.setResource(pearl);
        resourceInUser.setQuantity(20);
        return resourceInUser;
    }

    @NotNull
    public static ResourceInProductRequestDto getResourceInProductRequestDto(Pearl pearl) {
        ResourceInProductRequestDto resourceInProductRequestDto = new ResourceInProductRequestDto();
        resourceInProductRequestDto.setId(pearl.getId());
        resourceInProductRequestDto.setQuantity(5);
        return resourceInProductRequestDto;
    }

    @NotNull
    public static ResourceInProduct getResourceInProduct(Pearl pearl) {
        ResourceInProduct resourceInProduct = new ResourceInProduct();
        resourceInProduct.setResource(pearl);
        resourceInProduct.setQuantity(5);
        return resourceInProduct;
    }

    @NotNull
    public static Product getProduct(User user, List<ResourceInProduct> resourceInProduct) {
        Product testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setOwner(user);
        testProduct.setName("TestProduct");
        testProduct.setAuthors(List.of("Gosho"));
        testProduct.setSold(false);
        testProduct.setDescription("This is Test Product");
        testProduct.setSalePrice(1000);
        testProduct.setResourcesContent(resourceInProduct);
        testProduct.setProductsContent(null);
        testProduct.setContent(null);
        return testProduct;
    }
}
