package jewellery.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PearlDTO {
    private Long id;
    private String type;
    private double size;
    private String quality;
    private String shape;
}
