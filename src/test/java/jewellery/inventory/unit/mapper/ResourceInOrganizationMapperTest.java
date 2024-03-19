package jewellery.inventory.unit.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.helper.OrganizationTestHelper;
import jewellery.inventory.helper.ResourceInOrganizationTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.mapper.ResourceInOrganizationMapper;
import jewellery.inventory.mapper.ResourceMapper;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResourceInOrganizationMapperTest {

  @InjectMocks private ResourceInOrganizationMapper resourceInOrganizationMapper;
  @Mock private ResourceMapper resourceMapper;
  @Mock private OrganizationMapper organizationMapper;

  private Resource resource;
  private Organization organization;
  private ResourceInOrganization resourceInOrganization;

  @BeforeEach
  void setUp() {
    resource = ResourceTestHelper.getPearl();
    organization = OrganizationTestHelper.getTestOrganization();
    resourceInOrganization =
        ResourceInOrganizationTestHelper.createResourceInOrganization(organization, resource);
  }

  @Test
  void testMapEntityToResponse() {
    ResourcesInOrganizationResponseDto response =
        resourceInOrganizationMapper.toResourcesInOrganizationResponse(resourceInOrganization);

    assertNotNull(response);
    verify(organizationMapper, times(1)).toResponse(organization);
    verify(resourceMapper, times(1)).toResourceResponse(resource);
  }

  @Test
  void testMapOrganizationToResponse() {
    Resource secondResource = ResourceTestHelper.getMetal();
    ResourceInOrganization secondResourceInOrganization =
        ResourceInOrganizationTestHelper.createResourceInOrganization(organization, secondResource);
    organization.setResourceInOrganization(
        List.of(resourceInOrganization, secondResourceInOrganization));
    ResourcesInOrganizationResponseDto response =
        resourceInOrganizationMapper.toResourcesInOrganizationResponse(organization);

    assertEquals(2, response.getResourcesAndQuantities().size());
    verify(organizationMapper, times(1)).toResponse(organization);
  }

  @Test
  void testToResourcesInOrganizationResponseShouldReturnNullIfNullIsPassedAsArgument() {
    ResourcesInOrganizationResponseDto response =
        resourceInOrganizationMapper.toResourcesInOrganizationResponse(
            (ResourceInOrganization) null);

    assertNull(response);
    verify(organizationMapper, times(0)).toResponse(any());
  }
}
