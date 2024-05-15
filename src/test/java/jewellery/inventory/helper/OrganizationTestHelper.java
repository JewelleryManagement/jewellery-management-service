package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.util.*;

import io.micrometer.common.lang.Nullable;
import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.*;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import org.springframework.http.ResponseEntity;

public class OrganizationTestHelper {
  private static final String ORGANIZATION_NAME = "Test Name";
  private static final String ORGANIZATION_ADDRESS = "Test Note";
  private static final String ORGANIZATION_NOTE = "Test Note";

  public static Organization getTestOrganization() {
    Organization organization = new Organization();
    organization.setId(UUID.randomUUID());
    organization.setName(ORGANIZATION_NAME);
    organization.setAddress(ORGANIZATION_ADDRESS);
    organization.setNote(ORGANIZATION_NOTE);
    return organization;
  }

  public static Organization setProductAndResourcesToOrganization(Organization organization, Product product, ResourceInOrganization resourceInOrganization) {
    product.setOrganization(organization);
    organization.setProductsOwned(new ArrayList<>());
    organization.getProductsOwned().add(product);
    organization.setResourceInOrganization(List.of(resourceInOrganization));
    return organization;
  }

  public static Organization getOrganizationWithUserWithNoPermissions(
      Organization organization, User user) {
    organization.setUsersInOrganization(
        List.of(new UserInOrganization(UUID.randomUUID(), user, organization, new ArrayList<>())));
    return organization;
  }

  public static Organization getTestOrganizationWithUserWithAllPermissions(User user) {
    Organization organization = getTestOrganization();
    UserInOrganization userInOrganization =
        createUserInOrganizationAllPermissions(user, organization);
    List<UserInOrganization> usersInOrganizationList = new ArrayList<>();
    usersInOrganizationList.add(userInOrganization);
    organization.setUsersInOrganization(usersInOrganizationList);
    organization.setProductsOwned(new ArrayList<>());
    organization.setResourceInOrganization(new ArrayList<>());
    return organization;
  }

  private static UserInOrganization createUserInOrganizationAllPermissions(
      User user, Organization organization) {
    UserInOrganization userInOrganization = new UserInOrganization();
    userInOrganization.setId(UUID.randomUUID());
    userInOrganization.setOrganization(organization);
    userInOrganization.setUser(user);
    userInOrganization.setOrganizationPermission(List.of(OrganizationPermission.values()));

    return userInOrganization;
  }

  public static Organization getTestOrganizationWithUserInOrganizations() {
    Organization organization = getTestOrganization();
    UserInOrganization userInOrganization = new UserInOrganization();
    userInOrganization.setId(UUID.randomUUID());
    userInOrganization.setUser(new User());
    userInOrganization.setOrganization(organization);
    userInOrganization.setOrganizationPermission(List.of(OrganizationPermission.values()));
    organization.setUsersInOrganization(List.of(userInOrganization));
    return organization;
  }

  public static OrganizationRequestDto getTestOrganizationRequest() {
    OrganizationRequestDto organization = new OrganizationRequestDto();
    organization.setName(ORGANIZATION_NAME);
    organization.setAddress(ORGANIZATION_ADDRESS);
    organization.setNote(ORGANIZATION_NOTE);
    return organization;
  }

  public static UserInOrganizationRequestDto getTestUserInOrganizationRequest(UUID userId) {
    UserInOrganizationRequestDto request = new UserInOrganizationRequestDto();
    request.setUserId(userId);
    request.setOrganizationPermission(Arrays.asList(OrganizationPermission.values()));
    return request;
  }

  public static ExecutorResponseDto getTestExecutor(User user) {
    ExecutorResponseDto responseDto = new ExecutorResponseDto();
    responseDto.setId(user.getId());
    responseDto.setFirstName(user.getFirstName());
    responseDto.setLastName(user.getLastName());
    responseDto.setEmail(user.getEmail());
    return responseDto;
  }

  public static OrganizationResponseDto getTestOrganizationResponseDto(Organization organization) {
    OrganizationResponseDto responseDto = new OrganizationResponseDto();
    responseDto.setId(organization.getId());
    responseDto.setName(organization.getName());
    responseDto.setAddress(organization.getAddress());
    responseDto.setNote(organization.getNote());
    return responseDto;
  }

  public static UserInOrganization getTestUserInOrganization(Organization organization) {
    UserInOrganization dto = new UserInOrganization();
    dto.setOrganization(organization);
    dto.setId(UUID.randomUUID());
    dto.setUser(organization.getUsersInOrganization().get(0).getUser());
    dto.setOrganizationPermission(
        organization.getUsersInOrganization().get(0).getOrganizationPermission());
    return dto;
  }

  public static ResourceInOrganization createTestResourceInOrganization(Resource resource, Organization organization) {
    ResourceInOrganization resourceInOrganization = new ResourceInOrganization();
    resourceInOrganization.setResource(resource);
    resourceInOrganization.setId(UUID.randomUUID());
    resourceInOrganization.setQuantity(BigDecimal.ONE);
    resourceInOrganization.setDealPrice(BigDecimal.TEN);
    resourceInOrganization.setOrganization(organization);
    return resourceInOrganization;
  }
}
