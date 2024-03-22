package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.PurchasedResourceQuantityRequestDto;
import jewellery.inventory.dto.response.PurchasedResourceQuantityResponseDto;
import jewellery.inventory.model.PurchasedResourceInUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchasedResourceInUserMapper {

  @Mapping(source = "resourceAndQuantity.quantity", target = "quantity")
  PurchasedResourceInUser toPurchasedResourceInUser(
      PurchasedResourceQuantityRequestDto purchasedResourceQuantityRequestDto);

  @Mapping(source = "resource", target = "resourceAndQuantity.resource")
  @Mapping(source = "quantity", target = "resourceAndQuantity.quantity")
  PurchasedResourceQuantityResponseDto toPurchasedResourceQuantityResponseDto(PurchasedResourceInUser purchasedResourceInUser);
}
