package jewellery.inventory.unit.service;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.User;
import jewellery.inventory.repository.ProductRepository;
import jewellery.inventory.repository.ResourceInProductRepository;
import jewellery.inventory.repository.ResourceInUserRepository;
import jewellery.inventory.repository.UserRepository;
import jewellery.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @InjectMocks
    private ProductService productService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ResourceInUserRepository resourceInUserRepository;
    @Mock
    private ResourceInProductRepository resourceInProductRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {

    }
}
