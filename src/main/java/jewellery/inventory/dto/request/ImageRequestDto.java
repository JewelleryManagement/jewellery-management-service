package jewellery.inventory.dto.request;

import jewellery.inventory.config.FileSize;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageRequestDto {

  @FileSize(maxSizeInMB = 8, message = "File size must be up to 8 MB.")
  private MultipartFile image;
}
