package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.PurchasedResourceInUserRequestDto;
import jewellery.inventory.model.PurchasedResourceInUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchasedResourceInUserMapper {

  @Mapping(source = "resource.quantity", target = "quantity")
  PurchasedResourceInUser toPurchasedResourceInUser(
      PurchasedResourceInUserRequestDto purchasedResourceInUserRequestDto);
}
