package jewellery.inventory.service;

import static jewellery.inventory.mapper.ResourceMapper.toResourceEntity;
import static jewellery.inventory.mapper.ResourceMapper.toResourceResponse;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.notFoundException.ResourceNotFoundException;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {
  private final ResourceRepository resourceRepository;

  private ResourceResponseDto map(Resource resource) {
    return toResourceResponse(resource);
  }

  private Resource map(ResourceRequestDto resourceRequestDto) {
    return toResourceEntity(resourceRequestDto);
  }

  public List<ResourceResponseDto> getAllResources() {
    List<Resource> resources = resourceRepository.findAll();
    return resources.stream().map(this::map).toList();
  }

  public ResourceResponseDto createResource(ResourceRequestDto resourceRequestDto) {
    return map(resourceRepository.save(map(resourceRequestDto)));
  }

  public ResourceResponseDto getResourceById(UUID id) {
    Resource resource =
        resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    return map(resource);
  }

  public void deleteResourceById(UUID id) {
    Resource resource =
        resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    resourceRepository.delete(resource);
  }

  public ResourceResponseDto updateResource(UUID id, ResourceRequestDto resourceRequestDto) {
    resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    Resource toUpdate = toResourceEntity(resourceRequestDto);
    toUpdate.setId(id);
    return map(resourceRepository.save(toUpdate));
  }
}
