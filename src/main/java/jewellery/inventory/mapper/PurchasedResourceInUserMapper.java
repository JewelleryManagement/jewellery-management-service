package jewellery.inventory.mapper;

import jewellery.inventory.dto.request.PurchasedResourceInUserRequestDto;
import jewellery.inventory.dto.response.PurchasedResourceInUserResponseDto;
import jewellery.inventory.model.PurchasedResourceInUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PurchasedResourceInUserMapper {
  PurchasedResourceInUser toPurchasedResourceInUser(
      PurchasedResourceInUserRequestDto purchasedResourceInUserRequestDto);

  PurchasedResourceInUserResponseDto toPurchasedResourceInUserResponse(
      PurchasedResourceInUser purchasedResourceInUser);
}
