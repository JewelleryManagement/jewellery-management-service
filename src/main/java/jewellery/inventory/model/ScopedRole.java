package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.utils.PermissionConverter;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scoped_roles")
public class ScopedRole {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @ElementCollection(targetClass = Permission.class, fetch = FetchType.LAZY)
  @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
  @Column(name = "permission", nullable = false)
  @Convert(converter = PermissionConverter.class)
  private Set<Permission> permissions = new HashSet<>();
}
