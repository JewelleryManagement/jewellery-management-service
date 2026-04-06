package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganization;
import static jewellery.inventory.helper.SaleTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUserResponseDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.helper.ProductTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.OrganizationMembershipRepository;
import jewellery.inventory.service.OrganizationAuthorizationService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrganizationAuthorizationServiceTest {
  @InjectMocks private OrganizationAuthorizationService organizationAuthorizationService;
  @Mock private OrganizationMembershipRepository organizationMembershipRepository;
  @Mock private AuthService authService;

  private Organization organization;
  private UserResponseDto currentUser;
  private Permission permission;
  private Product product;
  private Sale sale;

  @BeforeEach
  void setUp() {
    organization = getTestOrganization();
    currentUser = createTestUserResponseDto(createTestUser());
    permission = Permission.ORGANIZATION_READ;
    Resource resource = ResourceTestHelper.getPearl();
    product = ProductTestHelper.getTestProduct(createTestUser(), resource);
    ProductPriceDiscount productPriceDiscount = createTestProductPriceDiscount(product, sale);
    PurchasedResourceInUser purchasedResource = createPurchasedResource(BigDecimal.TEN);
    sale =
        createSaleInOrganization(
            organization,
            createTestUser(),
            List.of(productPriceDiscount),
            List.of(purchasedResource));
  }

  @Test
  void hasOrganizationPermissionShouldThrowWhenUserNotFound() {
    when(authService.getCurrentUser()).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () ->
            organizationAuthorizationService.hasOrganizationPermission(
                organization.getId(), permission.name()));
  }

  @Test
  void hasOrganizationPermissionReturnFalseWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(organizationMembershipRepository.hasPermissionInOrganization(
            currentUser.getId(), organization.getId(), permission))
        .thenReturn(false);

    boolean hasPermission =
        organizationAuthorizationService.hasOrganizationPermission(
            organization.getId(), permission.name());

    assertFalse(hasPermission);
  }

  @Test
  void hasOrganizationPermissionReturnTrueWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(organizationMembershipRepository.hasPermissionInOrganization(
            currentUser.getId(), organization.getId(), permission))
        .thenReturn(true);

    boolean hasPermission =
        organizationAuthorizationService.hasOrganizationPermission(
            organization.getId(), permission.name());

    assertTrue(hasPermission);
  }

  @Test
  void hasPermissionForProductShouldThrowWhenUserNotFound() {
    when(authService.getCurrentUser()).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () ->
            organizationAuthorizationService.hasPermissionForProduct(
                product.getId(), permission.name()));
  }

  @Test
  void hasPermissionForProductReturnFalseWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(organizationMembershipRepository.hasAccessToProduct(
            product.getId(), currentUser.getId(), permission))
        .thenReturn(false);

    boolean hasPermission =
        organizationAuthorizationService.hasPermissionForProduct(
            product.getId(), permission.name());

    assertFalse(hasPermission);
  }

  @Test
  void hasPermissionForProductReturnTrueWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(organizationMembershipRepository.hasAccessToProduct(
            product.getId(), currentUser.getId(), permission))
        .thenReturn(true);

    boolean hasPermission =
        organizationAuthorizationService.hasPermissionForProduct(
            product.getId(), permission.name());

    assertTrue(hasPermission);
  }

  @Test
  void hasPermissionForSaleShouldThrowWhenUserNotFound() {
    when(authService.getCurrentUser()).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () ->
            organizationAuthorizationService.hasPermissionForSale(sale.getId(), permission.name()));
  }

  @Test
  void hasPermissionForSaleShouldReturnFalseWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(organizationMembershipRepository.hasAccessToSale(
            sale.getId(), currentUser.getId(), permission))
        .thenReturn(false);

    boolean hasPermission =
        organizationAuthorizationService.hasPermissionForSale(sale.getId(), permission.name());

    assertFalse(hasPermission);
  }

  @Test
  void hasPermissionForSaleShouldReturnTrueWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(organizationMembershipRepository.hasAccessToSale(
            sale.getId(), currentUser.getId(), permission))
        .thenReturn(true);

    boolean hasPermission =
        organizationAuthorizationService.hasPermissionForSale(sale.getId(), permission.name());

    assertTrue(hasPermission);
  }
}
