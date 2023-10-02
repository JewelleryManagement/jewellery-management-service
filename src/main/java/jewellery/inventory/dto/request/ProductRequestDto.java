package jewellery.inventory.dto.request;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    private String name;
    private List<String> authors;
    private String ownerName;
//    private byte[] picture;
    Map<String, Double> resourcesContent;
    private List<String> productsContent;

    private String description;
    private double salePrice;

}
