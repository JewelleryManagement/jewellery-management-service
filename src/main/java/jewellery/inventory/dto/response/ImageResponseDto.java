package jewellery.inventory.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageResponseDto {
    private String type;
    private String filePath;
    private UUID productId;
}
