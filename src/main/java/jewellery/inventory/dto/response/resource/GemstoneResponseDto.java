package jewellery.inventory.dto.response.resource;

import jewellery.inventory.model.resource.Gemstone;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class GemstoneResponseDto extends ResourceResponseDto {
  private String color;
  private double carat;
  private String cut;
  private String clarity;
  private String size;
  private String shape;

  public static GemstoneResponseDto toGemstoneResponseWithSize(Gemstone entity) {
    if (entity == null) {
      return null;
    }
    GemstoneResponseDto.GemstoneResponseDtoBuilder<?, ?> gemstoneResponseDto = GemstoneResponseDto.builder();

    gemstoneResponseDto.id(entity.getId());
    gemstoneResponseDto.clazz(entity.getClazz());
    gemstoneResponseDto.quantityType(entity.getQuantityType());
    String size = String.format("%.2fx%.2fx%.2f", entity.getDimensionX(), entity.getDimensionY(), entity.getDimensionZ());
    gemstoneResponseDto.size(size);
    gemstoneResponseDto.color(entity.getColor());
    gemstoneResponseDto.carat(entity.getCarat());
    gemstoneResponseDto.cut(entity.getCut());
    gemstoneResponseDto.clarity(entity.getClarity());
    gemstoneResponseDto.shape(entity.getShape());

    return gemstoneResponseDto.build();
  }
}
