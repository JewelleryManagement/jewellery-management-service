package jewellery.inventory.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "image")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue
    private UUID id;

    private String type;
    private String filePath;

    @OneToOne(mappedBy = "image")
    private Product product;
}
