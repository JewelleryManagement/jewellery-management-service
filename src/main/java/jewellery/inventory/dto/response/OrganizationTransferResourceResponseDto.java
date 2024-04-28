package jewellery.inventory.dto.response;

import lombok.*;
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationTransferResourceResponseDto {
  OrganizationResponseDto previousOwner;
  OrganizationResponseDto newOwner;
  ResourceQuantityResponseDto transferredResource;
}
