package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "image")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {
  public static final String FILE_NAME = "ProductPicture";
  @Id @GeneratedValue private UUID id;

  private String type;
  private String filePath;

  @OneToOne(mappedBy = "image")
  private Product product;
}
