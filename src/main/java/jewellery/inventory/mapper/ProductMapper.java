package jewellery.inventory.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.Product;
import jewellery.inventory.model.resource.ResourceInProduct;
import jewellery.inventory.utils.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

  private final UserMapper userMapper;
  private final ResourceMapper resourceMapper;
  private final OrganizationMapper organizationMapper;

  public ProductResponseDto mapToProductResponseDto(Product product) {

    ProductResponseDto productResponseDto = new ProductResponseDto();
    productResponseDto.setId(product.getId());
    if (product.getPartOfSale() != null) {
      productResponseDto.setPartOfSale(product.getPartOfSale().getSale().getId());
      productResponseDto.setSalePrice(getPriceFromSale(product));
    } else {
      productResponseDto.setSalePrice(calculateTotalPrice(product));
    }
    productResponseDto.setAuthors(getAuthorsResponse(product));
    productResponseDto.setDescription(product.getDescription());
    productResponseDto.setOwner(userMapper.toUserResponse(product.getOwner()));
    productResponseDto.setProductionNumber(product.getProductionNumber());
    productResponseDto.setCatalogNumber(product.getCatalogNumber());
    productResponseDto.setAdditionalPrice(
        BigDecimalUtil.getBigDecimal(product.getAdditionalPrice()));
    setContentProductToResponse(product, productResponseDto);
    setResourcesToResponse(product, productResponseDto);
    setProductsToResponse(product, productResponseDto);

    return productResponseDto;
  }

  public ProductsInOrganizationResponseDto mapToProductsInOrganizationResponseDto(
      Organization organization, List<ProductResponseDto> products) {
    ProductsInOrganizationResponseDto productsInOrganizationResponseDto =
        new ProductsInOrganizationResponseDto();

    productsInOrganizationResponseDto.setOrganization(organizationMapper.toResponse(organization));
    productsInOrganizationResponseDto.setProducts(products);
    return productsInOrganizationResponseDto;
  }

  private BigDecimal getPriceFromSale(Product product) {
    return product.getPartOfSale().getSalePrice();
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

  public static BigDecimal calculateTotalPrice(Product product) {
    return calculateTotalPrice(product, BigDecimal.ZERO);
  }

  private static BigDecimal calculateTotalPrice(Product product, BigDecimal totalPrice) {
    if (product.getResourcesContent() != null) {
      for (ResourceInProduct resource : product.getResourcesContent()) {
        BigDecimal resourcePrice =
            resource.getQuantity().multiply(resource.getResource().getPricePerQuantity());
        totalPrice = totalPrice.add(resourcePrice);
      }
    }

    if (product.getAdditionalPrice() != null) {
      totalPrice = totalPrice.add(product.getAdditionalPrice());
    }

    if (product.getProductsContent() != null) {
      for (Product nestedProduct : product.getProductsContent()) {
        totalPrice = calculateTotalPrice(nestedProduct, totalPrice);
      }
    }

    return BigDecimalUtil.getBigDecimal(totalPrice);
  }
}
