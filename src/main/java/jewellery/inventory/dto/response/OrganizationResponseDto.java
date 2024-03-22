package jewellery.inventory.dto.response;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {
  private UUID id;
  private String name;
  private String address;
  private String note;
}
