package jewellery.inventory.unit.mapper;

import static jewellery.inventory.helper.OrganizationTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.mapper.OrganizationMapper;
import jewellery.inventory.mapper.UserMapper;
import jewellery.inventory.model.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
 class OrganizationMapperTest {
  @InjectMocks private OrganizationMapper organizationMapper;
  @Mock private UserMapper userMapper;
  private Organization organization;
  private OrganizationRequestDto organizationRequestDto;

  @BeforeEach
  void setUp() {
    organization = getTestOrganizationWithUserInOrganizations();
    organizationRequestDto = getTestOrganizationRequest();
  }

  @Test
  void testMapRequestToEntity() {
    Organization newOrganization = organizationMapper.toEntity(organizationRequestDto);

    assertNotNull(newOrganization);
    Assertions.assertEquals(organizationRequestDto.getName(), newOrganization.getName());
    Assertions.assertEquals(organizationRequestDto.getAddress(), newOrganization.getAddress());
    Assertions.assertEquals(organizationRequestDto.getNote(), newOrganization.getNote());
  }
  @Test
  void testMapEntityToResponse() {
    OrganizationResponseDto responseDto = organizationMapper.toResponse(organization);

    assertNotNull(responseDto);
    Assertions.assertEquals(organizationRequestDto.getName(), responseDto.getName());
    Assertions.assertEquals(organizationRequestDto.getAddress(), responseDto.getAddress());
    Assertions.assertEquals(organizationRequestDto.getNote(), responseDto.getNote());
  }
}
