package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.ProductRequestDto;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.not_found.ProductWithoutResourcesException;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final UserMapper userMapper;
    private final ResourceMapper resourceMapper;

    public ProductResponseDto mapToProductResponseDto(Product product) {

        ProductResponseDto productResponseDto = new ProductResponseDto();
        productResponseDto.setId(product.getId());
        productResponseDto.setSold(product.isSold());
        productResponseDto.setAuthors(product.getAuthors());
        productResponseDto.setDescription(product.getDescription());
        productResponseDto.setSalePrice(product.getSalePrice());
        productResponseDto.setOwner(userMapper.toUserResponse(product.getOwner()));
        productResponseDto.setName(product.getName());

        setContentProductToResponse(product, productResponseDto);
        setResourcesToResponse(product, productResponseDto);
        setProductsToResponse(product, productResponseDto);

        return productResponseDto;
    }

    private void setProductsToResponse(Product product, ProductResponseDto response) {
        if (product.getProductsContent() == null) {
            response.setProductsContent(null);
        } else {
            response.setProductsContent(product.getProductsContent()
                    .stream().map(this::mapToProductResponseDto)
                    .toList());
        }
    }

    private void setResourcesToResponse(Product product, ProductResponseDto response) {
        if (product.getResourcesContent() == null) {
            throw new ProductWithoutResourcesException();
        } else {
            response.setResourcesContent(product.getResourcesContent()
                    .stream().map(res -> {
                        ResourceResponseDto resourceResponseDto = resourceMapper.toResourceResponseDto(res);

                        ResourceQuantityResponseDto resourceQuantityResponseDto = new ResourceQuantityResponseDto();
                        resourceQuantityResponseDto.setResource(resourceResponseDto);
                        resourceQuantityResponseDto.setQuantity(res.getQuantity());

                        return resourceQuantityResponseDto;
                    }).toList());
        }
    }

    private void setContentProductToResponse(Product product, ProductResponseDto response) {
        if (product.getContent() == null) {
            response.setContentOf(null);
        } else {
            response.setContentOf(product.getContent().getId());
        }
    }
}
