package jewellery.inventory.service;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.ResourceInProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.resource.ResourceInProductResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.NegativeResourceQuantityException;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.ProductContainsException;
import jewellery.inventory.exception.product.ProductIsSoldException;
import jewellery.inventory.mapper.UserMapper;
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
    private final UserMapper userMapper;


    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {

        Product product = new Product();
        User user = getUser(productRequestDto);
        List<ResourceInProduct> resourcesInProducts = getResourceInProducts(user, productRequestDto.getResourcesContent());

        product.setOwner(user);
        product.setName(productRequestDto.getName());
        product.setAuthors(productRequestDto.getAuthors());
        product.setSold(false);
        product.setDescription(productRequestDto.getDescription());
        product.setSalePrice(productRequestDto.getSalePrice());
        product.setResourcesContent(resourcesInProducts);

        productRepository.save(product);

        product.setProductsContent(getProductsInProduct(productRequestDto.getProductsContent(), product));
        productRepository.save(product);

        for (ResourceInProduct resourceInProduct : resourcesInProducts) {
            resourceInProduct.setProduct(product);
        }

        resourceInProductRepository.saveAll(resourcesInProducts);

        return mapToProductResponseDto(product);
    }

    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponseDto).toList();
    }

    public ProductResponseDto getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return mapToProductResponseDto(product);
    }

    public void deleteProduct(UUID id) {

        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException(id));

        if (!product.isSold()) {
            if (product.getContent() == null) {
                List<ResourceInProduct> resourcesInProduct = product.getResourcesContent();
                User owner = product.getOwner();
                List<ResourceInUser> resourcesInUser = destroyResourceInProductToResourceInUser(owner, resourcesInProduct);

                resourceInUserRepository.saveAll(resourcesInUser);

                if (product.getProductsContent() != null) {
                    for (Product contentProduct : product.getProductsContent()) {
                        contentProduct.setContent(null);
                        productRepository.save(contentProduct);
                    }

                    product.setProductsContent(new ArrayList<>());
                    productRepository.save(product);
                }

                productRepository.deleteById(id);
            } else {
                throw new ProductContainsException(id);
            }
        } else {
            throw new ProductIsSoldException(id);
        }
    }

    private List<ResourceInUser> destroyResourceInProductToResourceInUser(User owner, List<ResourceInProduct> resourcesInProduct) {
        List<ResourceInUser> resourcesInUser = owner.getResourcesOwned();

        for (ResourceInProduct resourceInProduct : resourcesInProduct) {
            Resource resource = resourceInProduct.getResource();

            ResourceInUser resourceInUser = resourceInUserRepository.findByResourceId(resource.getId());
            if (resourceInUser == null) {
                resourceInUser = new ResourceInUser();
                resourceInUser.setResource(resource);
                resourceInUser.setQuantity(resourceInProduct.getQuantity());
                resourceInUser.setOwner(owner);
            } else {
                resourceInUser.setQuantity(resourceInUser.getQuantity() + resourceInProduct.getQuantity());
            }

            resourcesInUser.add(resourceInUser);

            if (resourceInProduct.getProduct().getId() == null) {
                resourceInProductRepository.deleteById(resourceInProduct.getId());
            }
        }
        return resourcesInUser;
    }

    private User getUser(ProductRequestDto productRequestDto) {
        return userRepository.findById(productRequestDto.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException(productRequestDto.getOwnerId()));
    }

    private List<ResourceInProduct> getResourceInProducts(User user, List<ResourceInProductRequestDto> resourcesInProductRequestDto) {
        List<ResourceInUser> resourcesInUsers = user.getResourcesOwned();

        List<ResourceInProduct> resourcesInProducts = new ArrayList<>();

        ResourceInProduct resourceInProduct = new ResourceInProduct();

        for (ResourceInProductRequestDto resourceInProductRequestDto : resourcesInProductRequestDto) {
            Resource resource = resourceRepository.findById(resourceInProductRequestDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(resourceInProductRequestDto.getId()));

            double quantity = resourceInProductRequestDto.getQuantity();

            ResourceInUser resourceInUser = resourceInUserRepository.findByResourceId(resource.getId());

            if (!resourcesInUsers.contains(resourceInUser)) {
                throw new ResourceInUserNotFoundException(resource.getId(), user.getId());

            } else {
                if (resourceInUser.getQuantity() - quantity < 0) {
                    throw new NegativeResourceQuantityException(resourceInUser.getQuantity());
                } else if (resourceInUser.getQuantity() - quantity == 0) {
                    resourceInUserService.removeResourceFromUser(user.getId(), resource.getId());
                } else {
                    resourceInProduct.setResource(resource);
                    resourceInProduct.setQuantity(quantity);
                    resourcesInProducts.add(resourceInProduct);

                    resourceInUser.setQuantity(resourceInUser.getQuantity() - quantity);
                    resourceInUserRepository.save(resourceInUser);
                }
            }
        }

        return resourcesInProducts;
    }

    private List<Product> getProductsInProduct(List<UUID> productsIdInRequest, Product parentProduct) {
        List<Product> products = new ArrayList<>();

        if (productsIdInRequest != null) {
            productsIdInRequest.forEach(productId -> {

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));

                product.setContent(parentProduct);
                products.add(product);
            });
        }
        return products;
    }

    private ProductResponseDto mapToProductResponseDto(Product product) {
        ProductResponseDto response = new ProductResponseDto();
        response.setId(product.getId());
        response.setSold(product.isSold());
        response.setAuthors(product.getAuthors());
        response.setDescription(product.getDescription());
        response.setSalePrice(product.getSalePrice());
        response.setOwner(userMapper.toUserResponse(product.getOwner()));
        response.setName(product.getName());
        response.setContentId(product.getContent() == null ? null : product.getContent().getId());
        response.setResourcesContent(product.getResourcesContent() == null ? null : product.getResourcesContent()
                .stream().map(res -> {
                    ResourceInProductResponseDto resourceInProductResponseDto = new ResourceInProductResponseDto();
                    ResourceResponseDto resourceResponseDto = new ResourceResponseDto();
                    resourceResponseDto.setClazz(res.getResource() == null ? null : res.getResource().getClazz());
                    resourceResponseDto.setQuantityType(res.getResource() == null ? null : res.getResource().getQuantityType());
                    resourceResponseDto.setId(res.getResource() == null ? null : res.getResource().getId());
                    resourceInProductResponseDto.setResource(resourceResponseDto);
                    resourceInProductResponseDto.setQuantity(res.getQuantity());
                    return resourceInProductResponseDto;
                }).toList());
        response.setProductsContent(product.getProductsContent() == null
                ? null
                : product.getProductsContent()
                .stream().map(p -> getProduct(p.getId()))
                .toList());
        return response;
    }
}
