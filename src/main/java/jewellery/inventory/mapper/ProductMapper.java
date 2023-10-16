package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    productResponseDto.setAuthors(getAuthorsResponse(product));
    productResponseDto.setDescription(product.getDescription());
    productResponseDto.setSalePrice(product.getSalePrice());
    productResponseDto.setOwner(userMapper.toUserResponse(product.getOwner()));
    productResponseDto.setProductionNumber(product.getProductionNumber());
    productResponseDto.setCatalogNumber(product.getCatalogNumber());

    setContentProductToResponse(product, productResponseDto);
    setResourcesToResponse(product, productResponseDto);
    setProductsToResponse(product, productResponseDto);

    return productResponseDto;
  }

  private List<UserResponseDto> getAuthorsResponse(Product product) {
    List<User> authors = product.getAuthors();
    List<UserResponseDto> response = new ArrayList<>();
    authors.forEach(author -> response.add(userMapper.toUserResponse(author)));
    return response;
  }

  private void setProductsToResponse(Product product, ProductResponseDto response) {
    if (product.getProductsContent() == null) {
      response.setProductsContent(null);
    } else {
      response.setProductsContent(
          product.getProductsContent().stream().map(this::mapToProductResponseDto).toList());
    }
  }

  private void setResourcesToResponse(Product product, ProductResponseDto response) {

    response.setResourcesContent(
        product.getResourcesContent().stream()
            .map(
                res -> {
                  ResourceResponseDto resourceResponseDto =
                      resourceMapper.toResourceResponse(res.getResource());

                  ResourceQuantityResponseDto resourceQuantityResponseDto =
                      new ResourceQuantityResponseDto();
                  resourceQuantityResponseDto.setResource(resourceResponseDto);
                  resourceQuantityResponseDto.setQuantity(res.getQuantity());

                  return resourceQuantityResponseDto;
                })
            .toList());
  }

  private void setContentProductToResponse(Product product, ProductResponseDto response) {
    if (product.getContentOf() == null) {
      response.setContentOf(null);
    } else {
      response.setContentOf(product.getContentOf().getId());
    }
  }
}