package tdd.vendingMachine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.products.Product;
import tdd.vendingMachine.products.liquid.Liquid;
import tdd.vendingMachine.products.liquid.LiquidType;
import tdd.vendingMachine.products.snack.Snack;
import tdd.vendingMachine.products.snack.SnackType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests of initiating vending machine. Machine has multiple shelves, but one shelve holds the same product type.
 * Products can be of different types (i.e. Cola drink 0.25l, chocolate bar, mineral water 0.33l and so on).
 * Each product type has its own price.
 *
 * @author Łukasz Gadawski
 */
public class VendingMachineInitiatingTest {

    private VendingMachineConfig vendingMachineConfig;
    private VendingMachine vendingMachine;
    private List<Product> possibleProductTypeList;

    @Before
    public void init() {
        vendingMachineConfig =
            new VendingMachineConfig.Builder()
                .setMaxCoinNumberOfEachTypeInVendingMachine(100)
                .setNumberOfShelves(5)
                .setMaxProductsOnShelve(4)
                .setBundle("i18n.messages")
                .build();
        vendingMachine = new VendingMachine(vendingMachineConfig);

        Product p1 = new Liquid.Builder()
            .type(LiquidType.COKE)
            .price(BigDecimal.valueOf(2.5))
            .capacity(0.25)
            .build();
        Product p2 = new Liquid.Builder()
            .type(LiquidType.WATER)
            .price(BigDecimal.valueOf(1.3))
            .capacity(0.33)
            .build();
        Product p3 = new Snack.Builder()
            .type(SnackType.CHOCOLATE_BAR)
            .price(BigDecimal.valueOf(0.9))
            .weight(0.15)
            .build();

        possibleProductTypeList = Arrays.asList(p1, p2, p3);
        vendingMachine.putRandomProductsOnShelves(possibleProductTypeList);
    }

    @After
    public void reset() {
        vendingMachine.reset();
    }

    @Test
    public void testPuttingProductsOnShelves() {
        for (int i = 0; i < vendingMachineConfig.getNumberOfShelves(); i++) {
            assertThat(vendingMachine.getNumberOfProductsOnShelve(i))
                .isEqualTo(vendingMachineConfig.getMaxProductsOnShelve());
        }

        Product ex1 = vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 1);
        assertTrue(possibleProductTypeList.contains(ex1));

        Product ex2 = vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 2);
        assertTrue(possibleProductTypeList.contains(ex2));

        Product ex3 = vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 3);
        assertTrue(possibleProductTypeList.contains(ex3));
    }

    @Test
    public void testSameTypeOfProductOnShelve() {
        Map<Integer, List<Product>> shelves = vendingMachine.shelves();
        for (Map.Entry<Integer, List<Product>> entry : shelves.entrySet()) {
            List<Product> shelveProductList = entry.getValue();
            Product product = shelveProductList.get(0);
            if (product instanceof Snack) {
                for (Product p : shelveProductList) {
                    assertTrue(p instanceof Snack);
                }
            } else if (product instanceof Liquid) {
                for (Product p : shelveProductList) {
                    assertTrue(p instanceof Liquid);
                }
            }
        }
    }

}

