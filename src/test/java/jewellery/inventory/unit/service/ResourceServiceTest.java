package jewellery.inventory.unit.service;

import static jewellery.inventory.mapper.ResourceMapper.toResourceResponse;
import static jewellery.inventory.util.TestUtil.getGemstone;
import static jewellery.inventory.util.TestUtil.provideResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exeption.ApiRequestException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceRepository;
import jewellery.inventory.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {
  @Mock private ResourceRepository resourceRepository;
  @InjectMocks private ResourceService resourceService;

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndRequestDtos")
  void willSaveResource(Resource resourceFromDatabase, ResourceRequestDto resourceRequestDto) {
    when(resourceRepository.save(any())).thenReturn(resourceFromDatabase);
    ResourceResponseDto actualResourceResponseDto =
        resourceService.createResource(resourceRequestDto);

    assertEquals(resourceFromDatabase.getId(), actualResourceResponseDto.getId());
    verify(resourceRepository, times(1)).save(any());
  }

  @Test
  void willGetAllResources() {
    when(resourceRepository.findAll()).thenReturn(provideResources().toList());

    List<ResourceResponseDto> actualResourceResponseDtos = resourceService.getAllResources();

    assertEquals(provideResources().toList().size(), actualResourceResponseDtos.size());
    verify(resourceRepository, times(1)).findAll();
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndRequestDtos")
  void willGetAResource(Resource resourceFromDatabase, ResourceRequestDto expectedRequestDto) {
    when(resourceRepository.findById(resourceFromDatabase.getId()))
        .thenReturn(Optional.of(resourceFromDatabase));

    ResourceResponseDto actualResourceResponseDto =
        resourceService.getResourceById(resourceFromDatabase.getId());

    verify(resourceRepository, times(1)).findById(any());
    actualResourceResponseDto.setId(null);
    assertEquals(toResourceResponse(ResourceMapper.toResourceEntity(expectedRequestDto)), actualResourceResponseDto);
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideUpdatedResourcesAndUpdatedRequestDtos")
  void willUpdateAResource(Resource resourceFromDatabase, ResourceRequestDto expectedDto) {
    when(resourceRepository.findById(resourceFromDatabase.getId()))
        .thenReturn(Optional.of(resourceFromDatabase));
    when(resourceRepository.save(any())).thenReturn(resourceFromDatabase);

    ResourceResponseDto actualResourceResponseDto =
        resourceService.updateResource(resourceFromDatabase.getId(), expectedDto);

    verify(resourceRepository, times(1)).findById(any());
    verify(resourceRepository, times(1)).save(any());
    actualResourceResponseDto.setId(null);
    assertEquals(toResourceResponse(ResourceMapper.toResourceEntity(expectedDto)), actualResourceResponseDto);
  }

  @Test
  void willDeleteAResource() {
    Resource resource = getGemstone();
    when(resourceRepository.findById(any())).thenReturn(Optional.ofNullable(resource));

    resourceService.deleteResourceById(resource.getId());

    verify(resourceRepository, times(1)).delete(resource);
  }

  @Test
  void willThrowWhenDeleteANonExistingResource() {
    when(resourceRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(
        ApiRequestException.class, () -> resourceService.deleteResourceById(UUID.randomUUID()));
  }

  @Test
  void willThrowWhenUpdateANonExistingResource() {
    when(resourceRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(
        ApiRequestException.class,
        () ->
            resourceService.updateResource(
                UUID.randomUUID(), ResourceRequestDto.builder().build()));
  }

  @Test
  void willThrowWhenGetANonExistingResource() {
    when(resourceRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(
        ApiRequestException.class, () -> resourceService.getResourceById(UUID.randomUUID()));
  }
}
