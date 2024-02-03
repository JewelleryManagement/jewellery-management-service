package jewellery.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.repository.OrganizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;

  public List<Organization> all() {
    return organizationRepository.findAll();
  }

  public Optional<Organization> show(UUID id) {
    return organizationRepository.findById(id);
  }

  public OrganizationResponseDto create(OrganizationRequestDto organizationRequestDto) {
    Organization organization = new Organization();
    // TODO
    return null;
  }

}
