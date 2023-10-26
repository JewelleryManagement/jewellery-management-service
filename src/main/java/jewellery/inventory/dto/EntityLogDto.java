package jewellery.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityLogDto {
  private UUID id;
  private String message;
  private String timestamp;
  private String name;
  private String email;
  private String description;
  private String clazz;
  private String quantityType;
  private String color;
  private Double carat;
  private String cut;
  private String clarity;
  private Double dimensionX;
  private Double dimensionY;
  private Double dimensionZ;
  private String shape;
  private String type;
  private Double size;
  private String quality;
  private Integer purity;
  private String plating;
  private String nameBeforeUpdate;
  private String emailBeforeUpdate;
  private String descriptionBeforeUpdate;
  private String clazzBeforeUpdate;
  private String quantityTypeBeforeUpdate;
  private String colorBeforeUpdate;
  private Double caratBeforeUpdate;
  private String cutBeforeUpdate;
  private String clarityBeforeUpdate;
  private Double dimensionXBeforeUpdate;
  private Double dimensionYBeforeUpdate;
  private Double dimensionZBeforeUpdate;
  private String shapeBeforeUpdate;
  private String typeBeforeUpdate;
  private Double sizeBeforeUpdate;
  private String qualityBeforeUpdate;
  private Integer purityBeforeUpdate;
  private String platingBeforeUpdate;
}
