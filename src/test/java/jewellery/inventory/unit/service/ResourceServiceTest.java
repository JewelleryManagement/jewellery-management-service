package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ResourceTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.image.MultipartFileContentTypeException;
import jewellery.inventory.exception.image.MultipartFileNotSelectedException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
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
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {
  @Mock private ResourceRepository resourceRepository;
  @Mock private ResourceMapper resourceMapper;
  @InjectMocks private ResourceService resourceService;

  private MockMultipartFile file;
  private final String csvData =
      "clazz,quantityType,pricePerQuantity,note,description\nElement,28,30,smth,Element description\n";

  @ParameterizedTest
  @MethodSource("jewellery.inventory.helper.ResourceTestHelper#provideResourcesAndRequestDtos")
  void willSaveResource(Resource resourceFromDatabase, ResourceRequestDto resourceRequestDto) {

    when(resourceRepository.save(any())).thenReturn(resourceFromDatabase);

    Resource expectedResource = new Resource();
    expectedResource.setId(resourceFromDatabase.getId());
    when(resourceMapper.toResourceEntity(resourceRequestDto)).thenReturn(expectedResource);

    ResourceResponseDto expectedResponseDto = new ResourceResponseDto();
    expectedResponseDto.setId(resourceFromDatabase.getId());
    when(resourceMapper.toResourceResponse(resourceFromDatabase)).thenReturn(expectedResponseDto);

    ResourceResponseDto actualResourceResponseDto =
        resourceService.createResource(resourceRequestDto);

    assertEquals(expectedResponseDto.getId(), actualResourceResponseDto.getId());
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
  @MethodSource("jewellery.inventory.helper.ResourceTestHelper#provideResourcesAndRequestDtos")
  void willGetAResource(Resource resourceFromDatabase, ResourceRequestDto expectedRequestDto) {
    when(resourceRepository.findById(resourceFromDatabase.getId()))
        .thenReturn(Optional.of(resourceFromDatabase));

    ResourceResponseDto expectedResponseDto = new ResourceResponseDto();
    expectedResponseDto.setId(resourceFromDatabase.getId());
    when(resourceMapper.toResourceResponse(resourceFromDatabase)).thenReturn(expectedResponseDto);

    ResourceResponseDto actualResourceResponseDto =
        resourceService.getResource(resourceFromDatabase.getId());

    verify(resourceRepository, times(1)).findById(any());
    assertEquals(expectedResponseDto, actualResourceResponseDto);
  }

  @ParameterizedTest
  @MethodSource(
      "jewellery.inventory.helper.ResourceTestHelper#provideUpdatedResourcesAndUpdatedRequestDtos")
  void willUpdateAResource(Resource resourceFromDatabase, ResourceRequestDto expectedDto) {
    when(resourceRepository.findById(resourceFromDatabase.getId()))
        .thenReturn(Optional.of(resourceFromDatabase));

    Resource expectedResource = new Resource();
    expectedResource.setId(resourceFromDatabase.getId());
    when(resourceMapper.toResourceEntity(expectedDto)).thenReturn(expectedResource);

    when(resourceRepository.save(any())).thenReturn(resourceFromDatabase);

    ResourceResponseDto expectedResponseDto = new ResourceResponseDto();
    expectedResponseDto.setId(resourceFromDatabase.getId());
    when(resourceMapper.toResourceResponse(resourceFromDatabase)).thenReturn(expectedResponseDto);

    ResourceResponseDto actualResourceResponseDto =
        resourceService.updateResource(expectedDto, resourceFromDatabase.getId());

    verify(resourceRepository, times(1)).findById(any());
    verify(resourceRepository, times(1)).save(any());

    assertEquals(expectedResponseDto, actualResourceResponseDto);
  }

  @Test
  void willDeleteAResource() {
    Resource resource = getPreciousStone();
    when(resourceRepository.findById(any())).thenReturn(Optional.ofNullable(resource));

    resourceService.deleteResourceById(resource.getId());

    verify(resourceRepository, times(1)).delete(resource);
  }

  @Test
  void willThrowWhenDeleteANonExistingResource() {
    when(resourceRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> resourceService.deleteResourceById(UUID.randomUUID()));
  }

  @Test
  void willThrowWhenUpdateANonExistingResource() {
    when(resourceRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () ->
            resourceService.updateResource(
                ResourceRequestDto.builder().build(), UUID.randomUUID()));
  }

  @Test
  void willThrowWhenGetANonExistingResource() {
    when(resourceRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> resourceService.getResource(UUID.randomUUID()));
  }

  @Test
  void willThrowWhenImportFileIsNull() {
    assertThrows(
        MultipartFileNotSelectedException.class, () -> resourceService.importResources(file));
  }

  @Test
  void willThrowWhenImportFileIsEmpty() {
    file = new MockMultipartFile("empty.csv", "".getBytes());

    assertThrows(
        MultipartFileNotSelectedException.class, () -> resourceService.importResources(file));
  }

  @Test
  void willThrowWhenImportFileIsNotCsv() {
    file = new MockMultipartFile("file.txt", "file.txt", "text/plain", csvData.getBytes());

    assertThrows(
        MultipartFileContentTypeException.class, () -> resourceService.importResources(file));
  }

  @Test
  void willImportResources() {
    file = new MockMultipartFile("file.csv", "file.csv", "text/csv", csvData.getBytes());
    Resource element = getElement();
    when(resourceMapper.toResourceEntity(any(ResourceRequestDto.class))).thenReturn(element);
    when(resourceRepository.save(element)).thenReturn(element);

    resourceService.importResources(file);

    verify(resourceMapper, times(1)).toResourceEntity(any(ResourceRequestDto.class));
    verify(resourceRepository, times(1)).save(element);
    verify(resourceMapper, times(1)).toResourceResponse(element);
  }
}
