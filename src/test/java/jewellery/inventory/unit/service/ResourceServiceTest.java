package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ResourceTestHelper.getPreciousStone;
import static jewellery.inventory.helper.ResourceTestHelper.provideResources;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jewellery.inventory.dto.request.resource.ResourceRequestDto;
import jewellery.inventory.dto.response.PurchasedResourceInUserResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.exception.not_found.ResourceNotFoundException;
import jewellery.inventory.helper.SaleTestHelper;
import jewellery.inventory.helper.UserTestHelper;
import jewellery.inventory.mapper.PurchasedResourceInUserMapper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.PurchasedResourceInUser;
import jewellery.inventory.model.Sale;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.PurchasedResourceInUserRepository;
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
  @Mock private ResourceMapper resourceMapper;
  @Mock private PurchasedResourceInUserRepository purchasedResourceInUserRepository;
  @Mock private PurchasedResourceInUserMapper purchasedResourceInUserMapper;
  @InjectMocks private ResourceService resourceService;

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
  void testGetAllPurchasedResourcesSuccessfully() {
    User user = UserTestHelper.createTestUserWithId();

    PurchasedResourceInUser purchasedResourceInUser =
        SaleTestHelper.createPurchasedResource(BigDecimal.TEN);
    when(purchasedResourceInUserRepository.findAllByOwnerId(user.getId()))
        .thenReturn(List.of(purchasedResourceInUser));

    PurchasedResourceInUserResponseDto purchasedResourceInUserResponseDto =
        SaleTestHelper.createPurchasedResourceResponseDto(new Sale());
    when(purchasedResourceInUserMapper.toPurchaseResourceInUserResponse(any()))
        .thenReturn(purchasedResourceInUserResponseDto);

    List<PurchasedResourceInUserResponseDto> actualResponse =
        resourceService.getAllPurchasedResources(user.getId());

    assertNotNull(actualResponse);
    assertEquals(actualResponse.size(), 1);

    assertEquals(
        actualResponse.get(0).getResource().getResource().getId(),
        purchasedResourceInUserResponseDto.getResource().getResource().getId());
  }
}
