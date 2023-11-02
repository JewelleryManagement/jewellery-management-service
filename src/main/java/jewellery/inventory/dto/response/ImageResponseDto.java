package jewellery.inventory.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageResponseDto {
    private String name;
    private String type;
    private String filePath;
}
