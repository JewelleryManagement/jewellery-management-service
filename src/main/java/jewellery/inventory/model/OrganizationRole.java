package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organization_roles")
public class OrganizationRole {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @ElementCollection(targetClass = Permission.class, fetch = FetchType.LAZY)
  @CollectionTable(
      name = "organization_role_permissions",
      joinColumns = @JoinColumn(name = "role_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "permission", nullable = false)
  private Set<Permission> permissions = new HashSet<>();
}
