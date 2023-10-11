package jewellery.inventory.service;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.InsufficientResourceQuantityException;
import jewellery.inventory.exception.product.ProductWithoutResourcesException;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.ProductIsContentException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceInUserRepository resourceInUserRepository;
    private final ResourceInProductRepository resourceInProductRepository;
    private final ResourceInUserService resourceInUserService;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {

        User user = getUserFromRequest(productRequestDto);
        List<ResourceInProduct> resourcesInProduct = getResourcesFromRequest(user, productRequestDto.getResourcesContent());

        Product product = setFieldsToNewProduct(productRequestDto, user, resourcesInProduct);
        productRepository.save(product);

        setContentProduct(productRequestDto, product);
        setProductToResourcesInProduct(resourcesInProduct, product);

        return productMapper.mapToProductResponseDto(product);
    }

    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(productMapper::mapToProductResponseDto).toList();
    }

    public ProductResponseDto getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.mapToProductResponseDto(product);
    }

    public void deleteProduct(UUID id) {

        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException(id));

        throwExceptionIfProductIsSold(id, product);
        throwExceptionIfProductIsPartOfAnotherProduct(id, product);

        moveResourceInProductToResourceInUser(product);
        disassembleProductContent(product);

        productRepository.deleteById(id);
    }

    private void throwExceptionIfProductIsPartOfAnotherProduct(UUID id, Product product) {
        if (product.getContentOf() != null) {
            throw new ProductIsContentException(id);
        }
    }

    private void throwExceptionIfProductIsSold(UUID id, Product product) {
        if (product.isSold()) {
            throw new ProductIsSoldException(id);
        }
    }

    private void disassembleProductContent(Product product) {
        if (product.getProductsContent() != null) {
            product.getProductsContent().forEach(content -> {
                content.setContentOf(null);
                productRepository.save(content);
            });

            product.setProductsContent(new ArrayList<>());
            productRepository.save(product);
        }
    }

    private void moveResourceInProductToResourceInUser(Product product) {
        List<ResourceInProduct> resourcesInProduct = product.getResourcesContent();
        User owner = product.getOwner();

        List<ResourceInUser> resultResourcesInUser = new ArrayList<>();

        resourcesInProduct.forEach(resourceInProduct -> {
            Resource resource = resourceInProduct.getResource();
            ResourceInUser resourceInUser = moveResourcesToUserWhenDisassembleProduct(owner, resourceInProduct, resource);

            resultResourcesInUser.add(resourceInUser);

            if (resourceInProduct.getProduct() == null) {
                resourceInProductRepository.deleteById(resourceInProduct.getId());
            }
        });

        resourceInUserRepository.saveAll(resultResourcesInUser);
    }

    private User getUserFromRequest(ProductRequestDto productRequestDto) {
        return userRepository.findById(productRequestDto.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException(productRequestDto.getOwnerId()));
    }

    private List<ResourceInProduct> getResourcesFromRequest(User user, List<ResourceQuantityRequestDto> resourcesInProductRequestDto) {
        if (resourcesInProductRequestDto == null) {
            throw new ProductWithoutResourcesException();
        }
        List<ResourceInUser> resourcesInUsers = user.getResourcesOwned();
        if (resourcesInUsers == null) {
            throw new ResourceInUserNotFoundException(user.getId());
        }

        List<ResourceInProduct> resourcesInProducts = new ArrayList<>();
        ResourceInProduct resourceInProduct = new ResourceInProduct();

        resourcesInProductRequestDto.forEach(resourceQuantityRequestDto -> {
            Resource resource = resourceRepository.findById(resourceQuantityRequestDto.getId())
                    .orElseThrow(() -> new ResourceInUserNotFoundException(resourceQuantityRequestDto.getId()));
            ResourceInUser resourceInUser = resourceInUserRepository.findByResourceIdAndOwnerId(resource.getId(), user.getId());
            throwExceptionWhenResourceInUserNotExist(user, resourceInUser, resource, resourcesInUsers);

            double quantity = resourceQuantityRequestDto.getQuantity();
            setResourcesToProduct(resourceInUser, quantity, resourceInProduct, resource, resourcesInProducts);

            removeResourceFromUser(user, resourceInUser);

        });

        return resourcesInProducts;
    }

    private void throwExceptionWhenResourceInUserNotExist(User user, ResourceInUser resourceInUser, Resource resource, List<ResourceInUser> resourcesInUsers) {

        if (resourceInUser == null || !resourcesInUsers.contains(resourceInUser)) {
            throw new ResourceInUserNotFoundException(resource.getId(), user.getId());
        }
    }

    private void setResourcesToProduct(ResourceInUser resourceInUser, double quantity, ResourceInProduct resourceInProduct, Resource resource, List<ResourceInProduct> resourcesInProducts) {
        if (resourceInUser.getQuantity() < quantity) {
            throw new InsufficientResourceQuantityException(quantity, resourceInUser.getQuantity());
        } else {
            resourceInProduct.setResource(resource);
            resourceInProduct.setQuantity(quantity);
            resourcesInProducts.add(resourceInProduct);
            resourceInUser.setQuantity(resourceInUser.getQuantity() - quantity);
        }
    }

    private void removeResourceFromUser(User user, ResourceInUser resourceInUser) {
        if (resourceInUser.getQuantity() == 0) {
            resourceInUserService.removeResourceFromUser(user.getId(), resourceInUser.getResource().getId());
        }
    }

    private List<Product> getProductsInProduct(List<UUID> productsIdInRequest, Product parentProduct) {
        List<Product> products = new ArrayList<>();
        if (productsIdInRequest != null) {
            productsIdInRequest.forEach(productId -> {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));
                product.setContentOf(parentProduct);
                products.add(product);
            });
        }

        return products;
    }

    private ResourceInUser moveResourcesToUserWhenDisassembleProduct(User owner, ResourceInProduct resourceInProduct, Resource resource) {
        ResourceInUser resourceInUser = resourceInUserRepository.findByResourceId(resource.getId());
        if (resourceInUser == null) {
            resourceInUser = new ResourceInUser();
            resourceInUser.setResource(resource);
            resourceInUser.setQuantity(resourceInProduct.getQuantity());
            resourceInUser.setOwner(owner);
        } else {
            resourceInUser.setQuantity(resourceInUser.getQuantity() + resourceInProduct.getQuantity());
        }
        return resourceInUser;
    }

    private void setContentProduct(ProductRequestDto productRequestDto, Product product) {

        product.setProductsContent(getProductsInProduct(productRequestDto.getProductsContent(), product));
        productRepository.save(product);
    }

    private void setProductToResourcesInProduct(List<ResourceInProduct> resourcesInProducts, Product product) {
        resourcesInProducts.forEach(resourceInProduct -> resourceInProduct.setProduct(product));
        resourceInProductRepository.saveAll(resourcesInProducts);
    }

    private Product setFieldsToNewProduct(ProductRequestDto productRequestDto, User user, List<ResourceInProduct> resourcesInProducts) {
        Product product = new Product();

        product.setOwner(user);
        product.setName(productRequestDto.getName());
        product.setAuthors(productRequestDto.getAuthors());
        product.setSold(false);
        product.setDescription(productRequestDto.getDescription());
        product.setSalePrice(productRequestDto.getSalePrice());
        product.setResourcesContent(resourcesInProducts);
        return product;
    }
}