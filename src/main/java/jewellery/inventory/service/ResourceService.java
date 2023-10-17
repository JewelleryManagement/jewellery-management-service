package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {
  private final ResourceRepository resourceRepository;
  private final ResourceInUserRepository resourceInUserRepository;
  private final ResourceMapper resourceMapper;

  public List<ResourceResponseDto> getAllResources() {
    List<Resource> resources = resourceRepository.findAll();
    return resources.stream().map(resourceMapper::toResourceResponse).toList();
  }

  public ResourceResponseDto createResource(ResourceRequestDto resourceRequestDto) {
    Resource savedResource =
        resourceRepository.save(resourceMapper.toResourceEntity(resourceRequestDto));
    return resourceMapper.toResourceResponse(savedResource);
  }

  public ResourceResponseDto getResourceById(UUID id) {
    Resource resource =
        resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    return resourceMapper.toResourceResponse(resource);
  }

  public void deleteResourceById(UUID id) {
    Resource resource =
        resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    resourceRepository.delete(resource);
  }

  public ResourceResponseDto updateResource(UUID id, ResourceRequestDto resourceRequestDto) {
    resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    Resource toUpdate = resourceMapper.toResourceEntity(resourceRequestDto);
    toUpdate.setId(id);
    Resource updatedResource = resourceRepository.save(toUpdate);
    return resourceMapper.toResourceResponse(updatedResource);
  }

  public ResourceQuantityResponseDto getResourceQuantity(UUID id) {
    return ResourceQuantityResponseDto.builder()
        .quantity(resourceInUserRepository.sumQuantityByResource(id))
        .resource(
            resourceMapper.toResourceResponse(
                resourceRepository
                    .findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id))))
        .build();
  }

  public List<ResourceQuantityResponseDto> getAllResourceQuantities() {
    return resourceRepository.findAll().stream()
        .map(
            resource ->
                ResourceQuantityResponseDto.builder()
                    .resource(resourceMapper.toResourceResponse(resource))
                    .quantity(resourceInUserRepository.sumQuantityByResource(resource.getId()))
                    .build())
        .toList();
  }
}
