package jewellery.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.ResourceNotFoundException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResourceService {
  @Autowired private ResourceRepository resourceRepository;

  private ResourceResponseDto map(Resource resource) {
    return ResourceMapper.toResourceResponse(resource);
  }

  private Resource map(ResourceRequestDto resourceRequestDto) {
    return ResourceMapper.toResourceEntity(resourceRequestDto);
  }

  public List<ResourceResponseDto> getAllResources() {
    List<Resource> resources = resourceRepository.findAll();
    return resources.stream().map(this::map).toList();
  }

  public ResourceResponseDto createResource(ResourceRequestDto resourceRequestDto) {
    return map(resourceRepository.save(map(resourceRequestDto)));
  }

  public ResourceResponseDto getResourceById(UUID id) {
    Optional<Resource> resource = resourceRepository.findById(id);
    if (resource.isEmpty()) {
      throw new ResourceNotFoundException(id);
    }
    return map(resource.get());
  }

  public void deleteResourceById(UUID id) {
    Optional<Resource> resource = resourceRepository.findById(id);
    if (resource.isEmpty()) {
      throw new ResourceNotFoundException(id);
    }
    resourceRepository.delete(resource.get());
  }

  public ResourceResponseDto updateResource(UUID id, ResourceRequestDto resourceRequestDto) {
    Optional<Resource> findResource = resourceRepository.findById(id);
    if (findResource.isEmpty()) {
      throw new ResourceNotFoundException(id);
    }
    Resource toUpdate = ResourceMapper.toResourceEntity(resourceRequestDto);
    toUpdate.setId(id);
    return map(resourceRepository.save(toUpdate));
  }
}
