package jewellery.inventory.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ExecutorResponseDto {
  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
}
