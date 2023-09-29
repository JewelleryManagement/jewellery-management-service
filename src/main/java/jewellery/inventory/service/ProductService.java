package jewellery.inventory.service;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
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
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceInUserRepository resourceInUserRepository;
    private final ProductMapper productMapper;

    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {

        Product product = new Product();
        User user = getUser(productRequestDto, product);
        List<ResourceInProduct> resourcesInProducts = getResourceInProducts(user, productRequestDto.getResourcesContent());

        product.setOwner(user);
        product.setName(productRequestDto.getName());
        product.setAuthors(productRequestDto.getAuthors());
        product.setResourcesContent(resourcesInProducts);
        product.setProductsContent(getProducts(productRequestDto.getProductsContent()));
        product.setSold(false);
        product.setDescription(productRequestDto.getDescription());
        product.setSalePrice(productRequestDto.getSalePrice());

        productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    private User getUser(ProductRequestDto productRequestDto, Product product) {
        return userRepository.findByName(productRequestDto.getOwnerName())
                .orElseThrow(() -> new UserNotFoundException(productRequestDto.getOwnerName()));
    }

    private List<ResourceInProduct> getResourceInProducts(User user, List<String> resourcesInRequest) {
        List<ResourceInUser> resourcesInUsers = user.getResourcesOwned();

        List<ResourceInProduct> resourcesInProducts = new ArrayList<>();

        for (String resourceName : resourcesInRequest) {
            Resource resource = resourceRepository.findByClazz(resourceName)
                    .orElseThrow(() -> new ResourceNotFoundException(resourceName));

            ResourceInProduct resourceInProduct = new ResourceInProduct();
            boolean contains = false;

            for (ResourceInUser resourceInUser : resourcesInUsers) {
                if (resourceInUser.getId() == resource.getId()) {
                    resourceInProduct.setResource(resource);

                    double quantity = resourceInUser.getQuantity();
                    resourceInProduct.setQuantity(quantity);

                    resourceInUser.setQuantity(quantity);

                    resourcesInProducts.add(resourceInProduct);
                    resourceInUserRepository.save(resourceInUser);
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                throw new ResourceInUserNotFoundException(resource.getId(), user.getId());
            }
        }
        return resourcesInProducts;
    }

    private List<Product> getProducts(List<String> names) {
        List<Product> products = new ArrayList<>();

        for (String name : names) {
            Product current = productRepository.findByName(name)
                    .orElseThrow(() -> new ProductNotFoundException(name));

            products.add(current);
        }
        return products;
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.deleteById(id);
    }
}
