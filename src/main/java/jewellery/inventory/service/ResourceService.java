package jewellery.inventory.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.aspect.EntityFetcher;
import jewellery.inventory.aspect.annotation.LogCreateEvent;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.PurchasedResourceInUserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.EventType;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService implements EntityFetcher {
  private static final Logger logger = LogManager.getLogger(ResourceService.class);
  private final ResourceRepository resourceRepository;
  private final ResourceInUserRepository resourceInUserRepository;
  private final PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  private final ResourceMapper resourceMapper;
  private final PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  private final UserService userService;

  public List<ResourceResponseDto> getAllResources() {
    logger.debug("Fetching all Resources");
    List<Resource> resources = resourceRepository.findAll();
    return resources.stream().map(resourceMapper::toResourceResponse).toList();
  }

  @LogCreateEvent(eventType = EventType.RESOURCE_CREATE)
  public ResourceResponseDto createResource(ResourceRequestDto resourceRequestDto) {
    Resource savedResource =
        resourceRepository.save(resourceMapper.toResourceEntity(resourceRequestDto));
    logger.info("Resource created successfully. Resource ID: {}", savedResource.getId());
    return resourceMapper.toResourceResponse(savedResource);
  }

  public ResourceResponseDto getResource(UUID id) {
    Resource resource = getResourceById(id);
    logger.info("Resource fetched successfully. Resource ID: {}", resource.getId());
    return resourceMapper.toResourceResponse(resource);
  }

  @LogDeleteEvent(eventType = EventType.RESOURCE_DELETE)
  public void deleteResourceById(UUID id) {
    Resource resource = getResourceById(id);
    resourceRepository.delete(resource);
    logger.info("Resource deleted successfully. Resource ID: {}", id);
  }

  @LogUpdateEvent(eventType = EventType.RESOURCE_UPDATE)
  public ResourceResponseDto updateResource(ResourceRequestDto resourceRequestDto, UUID id) {
    resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    Resource toUpdate = resourceMapper.toResourceEntity(resourceRequestDto);
    toUpdate.setId(id);
    Resource updatedResource = resourceRepository.save(toUpdate);
    logger.info("Resource updated successfully. Resource ID: {}", updatedResource.getId());
    return resourceMapper.toResourceResponse(updatedResource);
  }

  public ResourceQuantityResponseDto getResourceQuantity(UUID id) {
    logger.debug("Fetching resource quantity by ID: {}", id);
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
    logger.debug("Fetching all resource quantities.");
    return resourceRepository.findAll().stream()
        .map(
            resource ->
                ResourceQuantityResponseDto.builder()
                    .resource(resourceMapper.toResourceResponse(resource))
                    .quantity(resourceInUserRepository.sumQuantityByResource(resource.getId()))
                    .build())
        .toList();
  }

  @Override
  public Object fetchEntity(Object... ids) {
    ids = Arrays.stream(ids).filter(UUID.class::isInstance).toArray();
    Resource resource = resourceRepository.findById((UUID) ids[0]).orElse(null);
    return resourceMapper.toResourceResponse(resource);
  }

  public Resource getResourceById(UUID id) {
    return resourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
  }

  public List<PurchasedResourceInUserResponseDto> getAllPurchasedResources(UUID userId) {
    User user = userService.getUser(userId);
    return purchasedResourceInUserRepository.findAllByOwnerId(user.getId()).stream()
        .map(purchasedResourceInUserMapper::toPurchaseResourceInUserResponse)
        .toList();
  }
}
