package jewellery.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class OrganizationSaleResponseDto {

    private UUID id;
    private OrganizationResponseDto organizationSeller;
    private UserResponseDto buyer;
    private List<ProductResponseDto> products;
    private List<PurchasedResourceQuantityResponseDto> resources;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate date;

    private BigDecimal totalPrice;
    private BigDecimal totalDiscountedPrice;
    private BigDecimal totalDiscount;
}