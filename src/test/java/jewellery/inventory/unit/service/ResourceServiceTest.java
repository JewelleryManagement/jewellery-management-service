package jewellery.inventory.unit.service;

import jewellery.inventory.dto.ResourceDTO;
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

import java.util.List;
import java.util.Optional;

import static jewellery.inventory.util.TestUtil.provideResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {
  @Mock private ResourceRepository resourceRepository;
  @InjectMocks private ResourceService resourceService;

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndDtos")
  void willSaveResource(Resource resourceFromDatabase, ResourceDTO inputResourceDTO) {
    when(resourceRepository.save(any())).thenReturn(resourceFromDatabase);
    inputResourceDTO.setId(null);
    ResourceDTO actualResourceDto = resourceService.createResource(inputResourceDTO);

    assertEquals(resourceFromDatabase.getId(), actualResourceDto.getId());
    verify(resourceRepository, times(1)).save(any());
  }

  @Test
  void willGetAllResources() {
    when(resourceRepository.findAll()).thenReturn(provideResources().toList());

    List<ResourceDTO> actualResourceDtos = resourceService.getAllResource();

    assertEquals(provideResources().toList().size(), actualResourceDtos.size());
    verify(resourceRepository, times(1)).findAll();
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndDtos")
  void willGetAResource(Resource resourceFromDatabase, ResourceDTO expectedDto) {
    when(resourceRepository.findById(resourceFromDatabase.getId()))
        .thenReturn(Optional.of(resourceFromDatabase));

    ResourceDTO actualResourceDto = resourceService.getResourceById(expectedDto.getId());

    verify(resourceRepository, times(1)).findById(any());
    assertEquals(expectedDto, actualResourceDto);
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.util.TestUtil#provideResourcesAndUpdatedDtos")
  void willUpdateAResource(Resource resourceFromDatabase, ResourceDTO expectedDto) {
    when(resourceRepository.findById(resourceFromDatabase.getId()))
        .thenReturn(Optional.of(resourceFromDatabase));
    when(resourceRepository.save(any())).thenReturn(resourceFromDatabase);
    ResourceDTO actualResourceDto =
        resourceService.updateResource(expectedDto.getId(), expectedDto);

    verify(resourceRepository, times(1)).findById(any());
    verify(resourceRepository, times(1)).save(any());
    assertEquals(expectedDto, actualResourceDto);
  }
}
