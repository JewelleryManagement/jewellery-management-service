package jewellery.inventory.services;

import java.util.List;
import java.util.Optional;
import jewellery.inventory.dto.ResourceDTO;
import jewellery.inventory.dto.ResourceInProductDTO;
import jewellery.inventory.dto.ResourceInUserDTO;
import jewellery.inventory.exeptions.ApiRequestException;
import jewellery.inventory.model.resources.Resource;
import jewellery.inventory.repositories.ResourceRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResourceService {
  private final ModelMapper modelMapper;
  private ResourceRepository resourceRepository;
  private List<ResourceInUserDTO> ResourceInUserDTO;
  private List<ResourceInProductDTO> productAffiliations;

  public ResourceDTO resourceToResourceDTO(Resource resource) {
    return modelMapper.map(resource, ResourceDTO.class);
  }

  public Resource resourceDTOtoResource(ResourceDTO resourceDTO) {
    return modelMapper.map(resourceDTO, Resource.class);
  }

  public List<ResourceDTO> getAllResource() {
    List<Resource> resources = resourceRepository.findAll();
    return resources.stream().map(this::resourceToResourceDTO).toList();
  }

  public ResourceDTO createResource(ResourceDTO resourceDTO) {
    resourceRepository.save(resourceDTOtoResource(resourceDTO));
    return resourceDTO;
  }

  public ResourceDTO getResourceById(Long id) {
    Optional<Resource> resource = resourceRepository.findById(id);
    if (resource.isEmpty()) {
      throw new ApiRequestException("This resource is not found");
    }
    return resourceToResourceDTO(resource.get());
  }

  public void deleteResourceById(Long id) {
    Optional<Resource> resource = resourceRepository.findById(id);
    if (resource.isEmpty()) {
      throw new ApiRequestException("Resource not found for id " + id);
    }
    resourceRepository.delete(resource.get());
  }

  public ResourceDTO updateResource(Long id, ResourceDTO resourceDTO) {
    Optional<Resource> findResource = resourceRepository.findById(id);
    if (findResource.isEmpty()) {
      throw new ApiRequestException("Resource not found");
    }
    findResource.get().setName(resourceDTO.getName());
    findResource.get().setQuantityType(resourceDTO.getQuantityType());

    resourceRepository.save(findResource.get());
    return resourceToResourceDTO(findResource.get());
  }
}
