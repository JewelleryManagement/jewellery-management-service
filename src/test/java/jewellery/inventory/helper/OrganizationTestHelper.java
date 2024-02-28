package jewellery.inventory.helper;

import java.util.*;

import jewellery.inventory.dto.request.OrganizationRequestDto;
import jewellery.inventory.dto.request.UpdateUserPermissionsRequest;
import jewellery.inventory.dto.request.UserInOrganizationRequestDto;
import jewellery.inventory.dto.response.ExecutorResponseDto;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.UserInOrganizationResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.OrganizationPermission;
import jewellery.inventory.model.User;
import jewellery.inventory.model.UserInOrganization;

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

  public static Organization getTestOrganizationWithUser(User user) {
    Organization organization = getTestOrganization();
    UserInOrganization userInOrganization = createUserInOrganization(user, organization);
    List<UserInOrganization> usersInOrganizationList = new ArrayList<>();
    usersInOrganizationList.add(userInOrganization);
    organization.setUsersInOrganization(usersInOrganizationList);
    return organization;
  }

  private static UserInOrganization createUserInOrganization(User user, Organization organization) {
    UserInOrganization userInOrganization = new UserInOrganization();
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

  public static UserInOrganizationResponseDto getTestUserInOrganizationResponseDto(
      Organization organization) {
    UserInOrganizationResponseDto dto = new UserInOrganizationResponseDto();
    dto.setUserId(organization.getUsersInOrganization().get(0).getUser().getId());
    dto.setOrganizationPermissions(
        organization.getUsersInOrganization().get(0).getOrganizationPermission());
    return dto;
  }
}
