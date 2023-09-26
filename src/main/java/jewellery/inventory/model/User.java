package jewellery.inventory.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
  @Id @GeneratedValue private UUID id;

  @Column(unique = true)
  private String name;

  @Column(unique = true)
  private String email;

  @Column(unique = true)
  private String password;

  @Column
  @Enumerated(EnumType.STRING)
  private Role role;

  // This field is essential for the application's business logic.
  // It is not marked as transient despite SonarQube's recommendation.
  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  private List<Product> productsOwned = new ArrayList<>();

  // This field is essential for the application's business logic.
  // It is not marked as transient despite SonarQube's recommendation.
  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ResourceInUser> resourcesOwned = new ArrayList<>();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
