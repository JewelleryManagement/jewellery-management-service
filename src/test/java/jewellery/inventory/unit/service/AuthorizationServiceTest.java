package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.OrganizationTestHelper.getTestOrganization;
import static jewellery.inventory.helper.SaleTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static jewellery.inventory.helper.UserTestHelper.createTestUserResponseDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import jewellery.inventory.dto.response.UserResponseDto;
import jewellery.inventory.exception.not_found.UserNotFoundException;
import jewellery.inventory.helper.ProductTestHelper;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.RoleMembershipRepository;
import jewellery.inventory.service.AuthorizationService;
import jewellery.inventory.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
  @InjectMocks private AuthorizationService authorizationService;
  @Mock private RoleMembershipRepository roleMembershipRepository;
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
  void hasSystemPermissionShouldThrowWhenUserNotFound() {
    when(authService.getCurrentUser()).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () -> authorizationService.hasSystemPermission(permission.name()));
  }

  @Test
  void hasSystemPermissionReturnFalseWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleMembershipRepository.hasSystemPermission(
            currentUser.getId(), RoleType.SYSTEM, permission))
        .thenReturn(false);

    boolean hasPermission = authorizationService.hasSystemPermission(permission.name());

    assertFalse(hasPermission);
    verify(roleMembershipRepository, times(1))
        .hasSystemPermission(currentUser.getId(), RoleType.SYSTEM, permission);
  }

  @Test
  void hasOrganizationPermissionShouldThrowWhenUserNotFound() {
    when(authService.getCurrentUser()).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () ->
            authorizationService.hasOrganizationPermission(
                organization.getId(), permission.name()));
  }

  @Test
  void hasOrganizationPermissionReturnFalseWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleMembershipRepository.hasPermissionInOrganization(
            currentUser.getId(), organization.getId(), permission))
        .thenReturn(false);

    boolean hasPermission =
        authorizationService.hasOrganizationPermission(organization.getId(), permission.name());

    assertFalse(hasPermission);
  }

  @Test
  void hasOrganizationPermissionReturnTrueWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleMembershipRepository.hasPermissionInOrganization(
            currentUser.getId(), organization.getId(), permission))
        .thenReturn(true);

    boolean hasPermission =
        authorizationService.hasOrganizationPermission(organization.getId(), permission.name());

    assertTrue(hasPermission);
  }

  @Test
  void hasPermissionForProductShouldThrowWhenUserNotFound() {
    when(authService.getCurrentUser()).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () -> authorizationService.hasPermissionForProduct(product.getId(), permission.name()));
  }

  @Test
  void hasPermissionForProductReturnFalseWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleMembershipRepository.hasAccessToProduct(
            product.getId(), currentUser.getId(), permission))
        .thenReturn(false);

    boolean hasPermission =
        authorizationService.hasPermissionForProduct(product.getId(), permission.name());

    assertFalse(hasPermission);
  }

  @Test
  void hasPermissionForProductReturnTrueWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleMembershipRepository.hasAccessToProduct(
            product.getId(), currentUser.getId(), permission))
        .thenReturn(true);

    boolean hasPermission =
        authorizationService.hasPermissionForProduct(product.getId(), permission.name());

    assertTrue(hasPermission);
  }

  @Test
  void hasPermissionForSaleShouldThrowWhenUserNotFound() {
    when(authService.getCurrentUser()).thenThrow(UserNotFoundException.class);

    assertThrows(
        UserNotFoundException.class,
        () -> authorizationService.hasPermissionForSale(sale.getId(), permission.name()));
  }

  @Test
  void hasPermissionForSaleShouldReturnFalseWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleMembershipRepository.hasAccessToSale(sale.getId(), currentUser.getId(), permission))
        .thenReturn(false);

    boolean hasPermission =
        authorizationService.hasPermissionForSale(sale.getId(), permission.name());

    assertFalse(hasPermission);
  }

  @Test
  void hasPermissionForSaleShouldReturnTrueWhenCurrentUserHasNoPermission() {
    when(authService.getCurrentUser()).thenReturn(currentUser);
    when(roleMembershipRepository.hasAccessToSale(sale.getId(), currentUser.getId(), permission))
        .thenReturn(true);

    boolean hasPermission =
        authorizationService.hasPermissionForSale(sale.getId(), permission.name());

    assertTrue(hasPermission);
  }
}
