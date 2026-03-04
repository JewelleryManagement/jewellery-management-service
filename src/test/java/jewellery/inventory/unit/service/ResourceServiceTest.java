package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ResourceTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.duplicate.DuplicateException;
import jewellery.inventory.exception.image.MultipartFileContentTypeException;
import jewellery.inventory.exception.image.MultipartFileNotSelectedException;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.exception.resource.ResourceInUseException;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.ResourceInOrganizationRepository;
import jewellery.inventory.repository.ResourceInProductRepository;
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
  @Mock private ResourceInOrganizationRepository resourceInOrganizationRepository;
  @Mock private ResourceInProductRepository resourceInProductRepository;
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
    Resource resource = getDiamond();
    when(resourceRepository.findById(any())).thenReturn(Optional.ofNullable(resource));
    when(resourceInOrganizationRepository.existsByResourceId(resource.getId())).thenReturn(false);
    when(resourceInProductRepository.existsByResourceId(resource.getId())).thenReturn(false);

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
  void willThrowWhenDeleteResourcePartOfOrganization() {
    Resource resource = getDiamond();
    when(resourceRepository.findById(any())).thenReturn(Optional.ofNullable(resource));
    when(resourceInOrganizationRepository.existsByResourceId(resource.getId())).thenReturn(true);

    ResourceInUseException exception =
        assertThrows(
            ResourceInUseException.class,
            () -> resourceService.deleteResourceById(resource.getId()));

    assertEquals(
        "Resource with id: " + resource.getId() + " is part of organization!",
        exception.getMessage());
  }

  @Test
  void willThrowWhenDeleteResourcePartOfProduct() {
    Resource resource = getDiamond();
    when(resourceRepository.findById(any())).thenReturn(Optional.ofNullable(resource));
    when(resourceInProductRepository.existsByResourceId(resource.getId())).thenReturn(true);

    ResourceInUseException exception =
        assertThrows(
            ResourceInUseException.class,
            () -> resourceService.deleteResourceById(resource.getId()));

    assertEquals(
        "Resource with id: " + resource.getId() + " is part of product!", exception.getMessage());
  }

  @Test
  void willThrowWhenDeleteResourcePartOfOrganizationAndProduct() {
    Resource resource = getDiamond();
    when(resourceRepository.findById(any())).thenReturn(Optional.ofNullable(resource));
    when(resourceInOrganizationRepository.existsByResourceId(resource.getId())).thenReturn(true);
    when(resourceInProductRepository.existsByResourceId(resource.getId())).thenReturn(true);

    ResourceInUseException exception =
        assertThrows(
            ResourceInUseException.class,
            () -> resourceService.deleteResourceById(resource.getId()));

    assertEquals(
        "Resource with id: " + resource.getId() + " is part of organization and product!",
        exception.getMessage());
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

  @ParameterizedTest
  @MethodSource("jewellery.inventory.helper.ResourceTestHelper#provideResourcesAndRequestDtos")
  void willThrowWhenCreateResourceWithExistingSku(
      Resource resourceFromDatabase, ResourceRequestDto resourceRequestDto) {
    when(resourceRepository.existsBySku(resourceRequestDto.getSku())).thenReturn(true);

    DuplicateException exception =
        assertThrows(
            DuplicateException.class, () -> resourceService.createResource(resourceRequestDto));

    assertEquals(
        "Stock Keeping Unit: " + resourceRequestDto.getSku() + " already exists!",
        exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("jewellery.inventory.helper.ResourceTestHelper#provideResourcesAndRequestDtos")
  void willThrowWhenUpdateResourceWithExistingSku(
      Resource resourceFromDatabase, ResourceRequestDto resourceRequestDto) {
    Resource resource = new Resource();
    UUID id = UUID.randomUUID();
    when(resourceRepository.findById(id)).thenReturn(Optional.of(resource));
    when(resourceRepository.existsBySkuAndIdNot(resourceRequestDto.getSku(), id)).thenReturn(true);

    DuplicateException exception =
        assertThrows(
            DuplicateException.class, () -> resourceService.updateResource(resourceRequestDto, id));

    assertEquals(
        "Stock Keeping Unit: " + resourceRequestDto.getSku() + " already exists!",
        exception.getMessage());
  }
}
