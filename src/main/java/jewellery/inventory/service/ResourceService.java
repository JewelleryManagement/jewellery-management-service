package jewellery.inventory.service;

import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.ResourceQuantityDto;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.EventType;
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

  @LogCreateEvent(eventType = EventType.RESOURCE_CREATE)
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

  @LogDeleteEvent(eventType = EventType.RESOURCE_DELETE)
  public void deleteResourceById(UUID id) {
    Resource resource =
        resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    resourceRepository.delete(resource);
  }

  @LogUpdateEvent(eventType = EventType.RESOURCE_UPDATE)
  public ResourceResponseDto updateResource(ResourceRequestDto resourceRequestDto, UUID id) {
    resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    Resource toUpdate = resourceMapper.toResourceEntity(resourceRequestDto);
    toUpdate.setId(id);
    Resource updatedResource = resourceRepository.save(toUpdate);
    return resourceMapper.toResourceResponse(updatedResource);
  }

  public ResourceQuantityDto getResourceQuantity(UUID id) {
    return ResourceQuantityDto.builder()
        .quantity(resourceInUserRepository.sumQuantityByResource(id))
        .resource(
            resourceMapper.toResourceResponse(
                resourceRepository
                    .findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id))))
        .build();
  }

  public List<ResourceQuantityDto> getAllResourceQuantities() {
    return resourceRepository.findAll().stream()
        .map(
            resource ->
                ResourceQuantityDto.builder()
                    .resource(resourceMapper.toResourceResponse(resource))
                    .quantity(resourceInUserRepository.sumQuantityByResource(resource.getId()))
                    .build())
        .toList();
  }

  public ResourceResponseDto fetchByIdAsDto(UUID id) {
    Resource resource =
        resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    return resourceMapper.toResourceResponse(resource);
  }
}
