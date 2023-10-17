package jewellery.inventory.model.resource;

import jakarta.persistence.Entity;
import jewellery.inventory.dto.response.resource.GemstoneResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Gemstone extends Resource {
  private String color;
  private double carat;
  private String cut;
  private String clarity;
  private double dimensionX;
  private double dimensionY;
  private double dimensionZ;
  private String shape;

    public GemstoneResponseDto toResourceResponse() {
    GemstoneResponseDto responseDto = new GemstoneResponseDto();
    responseDto.setColor(this.color);
    responseDto.setCarat(this.carat);
    responseDto.setCut(this.cut);
    responseDto.setClarity(this.clarity);
    responseDto.setShape(this.shape);
    String size = String.format("%.2f x %.2f x %.2f", this.dimensionX, this.dimensionY, this.dimensionZ);
    responseDto.setSize(size);
    responseDto.setClazz(this.getClazz());
    responseDto.setQuantityType(this.getQuantityType());
    return responseDto;
  }


}
