package jewellery.inventory.service;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.invalid_resource_quantity.NegativeResourceQuantityException;
import jewellery.inventory.exception.not_found.ProductNotFoundException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.ResourceInUser;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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


    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {

        Product product = new Product();
        User user = getUser(productRequestDto, product);
        List<ResourceInProduct> resourcesInProducts = getResourceInProducts(user, productRequestDto.getResourcesContent());

        product.setOwner(user);
        product.setName(productRequestDto.getName());
        product.setAuthors(productRequestDto.getAuthors());
        product.setResourcesContent(resourcesInProducts);
        product.setProductsContent(getProductsInProduct(productRequestDto.getProductsContent()));
        product.setSold(false);
        product.setDescription(productRequestDto.getDescription());
        product.setSalePrice(productRequestDto.getSalePrice());

        productRepository.save(product);

        for (ResourceInProduct resourceInProduct : resourcesInProducts) {
            resourceInProduct.setProduct(product);
        }

        resourceInProductRepository.saveAll(resourcesInProducts);
        return productMapper.toProductResponse(product);
    }

    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(productMapper::toProductResponse).collect(Collectors.toList());
    }

    public ProductResponseDto getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toProductResponse(product);
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException(id));

        List<ResourceInProduct> resourcesInProduct = product.getResourcesContent();
        User owner = product.getOwner();
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
            resourceInProductRepository.deleteById(resourceInProduct.getId());
        }

        resourceInUserRepository.saveAll(resourcesInUser);
        productRepository.deleteById(id);
    }

    private User getUser(ProductRequestDto productRequestDto, Product product) {
        return userRepository.findByName(productRequestDto.getOwnerName())
                .orElseThrow(() -> new UserNotFoundException(productRequestDto.getOwnerName()));
    }

    private List<ResourceInProduct> getResourceInProducts(User user, Map<String, Double> resourcesInRequest) {
        List<ResourceInUser> resourcesInUsers = user.getResourcesOwned();

        List<ResourceInProduct> resourcesInProducts = new ArrayList<>();

        ResourceInProduct resourceInProduct = new ResourceInProduct();

        for (Map.Entry<String, Double> entry : resourcesInRequest.entrySet()) {
            Resource resource = resourceRepository.findByClazz(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException(entry.getKey()));

            double quantity = entry.getValue();
            boolean contains = false;

            for (ResourceInUser resourceInUser : resourcesInUsers) {
                if (resourceInUser.getResource().getId() == resource.getId()) {
                    resourceInProduct.setResource(resource);
                    resourceInProduct.setQuantity(quantity);

                    resourceInUser.setQuantity(resourceInUser.getQuantity() - quantity);

                    if (resourceInUser.getQuantity() < 0) {
                        throw new NegativeResourceQuantityException(resourceInUser.getQuantity());
                    } else if (resourceInUser.getQuantity() == 0) {
                        resourceInUserService.removeResourceFromUser(user.getId(), resource.getId());
                    } else {
                        resourceInUserRepository.save(resourceInUser);
                        resourcesInProducts.add(resourceInProduct);
                        resourceInProductRepository.save(resourceInProduct);
                    }

                    contains = true;
                }
            }
            if (!contains) {
                throw new ResourceInUserNotFoundException(resource.getId(), user.getId());
            }
        }

        return resourcesInProducts;
    }

    private List<Product> getProductsInProduct(List<String> productsNamesInRequest) {
        List<Product> products = new ArrayList<>();

        if (productsNamesInRequest != null) {
            for (String name : productsNamesInRequest) {
                Product product = productRepository.findByName(name)
                        .orElseThrow(() -> new ProductNotFoundException(name));

                products.add(product);
            }
        }
        return products;
    }
}
