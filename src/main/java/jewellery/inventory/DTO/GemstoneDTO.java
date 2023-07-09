package jewellery.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GemstoneDTO {
    private Long id;
    private String color;
    private double carat;
    private String cut;
    private String clarity;
    private double dimensionX;
    private double dimensionY;
    private double dimensionZ;
    private String shape;


}

