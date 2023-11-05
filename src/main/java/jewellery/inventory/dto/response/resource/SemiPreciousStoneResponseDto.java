package jewellery.inventory.dto.response.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class SemiPreciousStoneResponseDto extends ResourceResponseDto{
    private String color;
    private String cut;
    private String clarity;
    private String shape;
    private Double size;
}
