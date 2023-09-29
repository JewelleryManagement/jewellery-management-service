package jewellery.inventory.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    private String name;
    private List<String> authors;
    private String ownerName;
//    private byte[] picture;
    private List<String> resourcesContent;
    private List<String> productsContent;

    private String description;
    private double salePrice;

}
