package jewellery.inventory.mapper;

import java.time.LocalDate;
import java.util.List;

import jewellery.inventory.calculator.ProductPriceCalculator;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.Product;
import jewellery.inventory.repository.ProductPriceDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

  private final UserMapper userMapper;
  private final ResourceMapper resourceMapper;
  private final ProductPriceDiscountRepository productPriceDiscountRepository;
  public ProductResponseDto mapToProductResponseDto(Product product) {

    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(product.getId());
    if (product.getPartOfSale() != null && !product.getPartOfSale().getProducts().isEmpty()) {
      productResponseDto.setPartOfSale(product.getPartOfSale().getId());
      productResponseDto.setSalePrice(
          productPriceDiscountRepository
              .findBySaleIdAndProductId(product.getPartOfSale().getId(), product.getId())
              .getSalePrice());
    } else {
      productResponseDto.setSalePrice(ProductPriceCalculator.calculateTotalPrice(product));
    }
    productResponseDto.setAuthors(getAuthorsResponse(product));
    productResponseDto.setDescription(product.getDescription());
    productResponseDto.setOwner(userMapper.toUserResponse(product.getOwner()));
    productResponseDto.setProductionNumber(product.getProductionNumber());
    productResponseDto.setCatalogNumber(product.getCatalogNumber());
    productResponseDto.setAdditionalPrice(product.getAdditionalPrice());

    setContentProductToResponse(product, productResponseDto);
    setResourcesToResponse(product, productResponseDto);
    setProductsToResponse(product, productResponseDto);

    return productResponseDto;
  }

  private List<UserResponseDto> getAuthorsResponse(Product product) {
    return product.getAuthors().stream().map(userMapper::toUserResponse).toList();
  }

  private void setProductsToResponse(Product product, ProductResponseDto response) {
    if (product.getProductsContent() != null) {
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

  public ProductReturnResponseDto mapToProductReturnResponseDto(
      SaleResponseDto sale, Product product) {
    ProductReturnResponseDto returnedProductResponseDto = new ProductReturnResponseDto();
    returnedProductResponseDto.setReturnedProduct(mapToProductResponseDto(product));
    returnedProductResponseDto.setSaleAfter(sale);
    returnedProductResponseDto.setDate(LocalDate.now());
    return returnedProductResponseDto;
  }

  private void setContentProductToResponse(Product product, ProductResponseDto response) {
    if (product.getContentOf() != null) {
      response.setContentOf(product.getContentOf().getId());
    }
  }
}
