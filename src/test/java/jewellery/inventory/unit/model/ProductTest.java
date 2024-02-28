package jewellery.inventory.unit.model;

import jewellery.inventory.model.Product;
import jewellery.inventory.model.ProductPriceDiscount;
import jewellery.inventory.model.User;
import jewellery.inventory.model.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static jewellery.inventory.helper.ProductTestHelper.getTestProduct;
import static jewellery.inventory.helper.UserTestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {
    private Product testProductOne;
    private Product testProductTwo;
    private Product testProductThree;
    private ProductPriceDiscount productPriceDiscount;

    @BeforeEach
    void setUp() {
        User user = createTestUser();
        testProductOne = getTestProduct(user, new Resource());
        testProductTwo = getTestProduct(user, new Resource());
        testProductThree = getTestProduct(user, new Resource());

        testProductTwo.setContentOf(testProductOne);
        testProductThree.setContentOf(testProductTwo);

        productPriceDiscount = new ProductPriceDiscount();
    }

    @Test
    void testGetPartOfSaleShouldReturnNullWhenSetPartOfSaleNeverInvoked() {
        assertNull(testProductOne.getPartOfSale());
    }

    @Test
    void testGetPartOfSaleShouldReturnNullWhenInvokedFromProductWithNonSoldContentOf() {
        assertNull(testProductTwo.getPartOfSale());
    }

    @Test
    void testGetPartOfSaleShouldReturnNullWhenInvokedFromProductWithNonSoldContentOfNested() {
        assertNull(testProductThree.getPartOfSale());
    }

    @Test
    void testGetPartOfSaleShouldReturnSaleWhenProductIsSold() {
        testProductOne.setPartOfSale(productPriceDiscount);

        assertEquals(testProductOne.getPartOfSale(), productPriceDiscount);
        assertNotNull(testProductOne.getPartOfSale());
    }

    @Test
    void testGetPartOfSaleShouldReturnSaleWhenContentOfIsSold() {
        testProductOne.setPartOfSale(productPriceDiscount);

        assertEquals(testProductTwo.getPartOfSale(), productPriceDiscount);
        assertNotNull(testProductTwo.getPartOfSale());
    }

    @Test
    void testGetPartOfSaleShouldReturnSaleWhenContentOfIsSoldNested() {
        testProductOne.setPartOfSale(productPriceDiscount);

        assertEquals(testProductThree.getPartOfSale(), productPriceDiscount);
        assertNotNull(testProductThree.getPartOfSale());
    }
}
