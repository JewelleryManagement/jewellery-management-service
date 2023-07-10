package jewellery.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreciousMetalDTO {
    private Long id;
    private String type;
    private int purity;
    private String color;
    private String plating;
}
