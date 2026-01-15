package jewellery.inventory.unit.service;

import static jewellery.inventory.helper.ProductTestHelper.*;
import static jewellery.inventory.helper.UserTestHelper.createTestUserWithRandomId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import jewellery.inventory.dto.response.ProductResponseDto;
import jewellery.inventory.exception.not_found.*;
import jewellery.inventory.exception.product.*;
import jewellery.inventory.helper.ResourceTestHelper;
import jewellery.inventory.mapper.ProductMapper;
import jewellery.inventory.model.*;
import jewellery.inventory.model.resource.Resource;
import jewellery.inventory.repository.*;
import jewellery.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @InjectMocks private ProductService productService;
  @Mock private ProductRepository productRepository;
  @Mock private ProductMapper productMapper;
  @Mock private UserRepository userRepository;

  private User user;
  private Product product;
  private Resource pearl;

  @BeforeEach
  void setUp() {
    user = createTestUserWithRandomId();
    pearl = ResourceTestHelper.getPearl();
    product = getTestProduct(user, pearl);
  }

  @Test
  void testGetProductShouldThrowWhenProductNotFound() {
    UUID fakeId = UUID.fromString("58bda8d1-3b3d-4319-922b-f5bb66623d71");
    assertThrows(ProductNotFoundException.class, () -> productService.getProductResponse(fakeId));
  }

  @Test
  void testGetProductWhenProductFound() {

    when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

    ProductResponseDto response = new ProductResponseDto();
    when(productMapper.mapToProductResponseDto(any())).thenReturn(response);

    ProductResponseDto actual = productService.getProductResponse(product.getId());

    assertEquals(response, actual);
    assertEquals(response.getId(), actual.getId());
    assertEquals(response.getAuthors(), actual.getAuthors());
    assertEquals(response.getCatalogNumber(), actual.getCatalogNumber());
  }
}
