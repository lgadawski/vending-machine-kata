package tdd.vendingMachine;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.facade.admin.AdminVendingMachineFacadeImpl;
import tdd.vendingMachine.products.Product;
import tdd.vendingMachine.products.liquid.Liquid;
import tdd.vendingMachine.products.liquid.LiquidType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for administration tasks on vending machine.
 *
 * @author Łukasz Gadawski
 */
public class AdminVendingMachineFacadeTest {

    private AdminVendingMachineFacadeImpl admin;
    private VendingMachine machine;

    @Before
    public void init() {
        VendingMachineConfig config = new VendingMachineConfig.Builder()
            .setMaxCoinNumberOfEachTypeInVendingMachine(10)
            .setNumberOfShelves(3)
            .setMaxProductsOnShelve(4)
            .setBundle("i18n.messages")
            .build();

        machine = new VendingMachine(config);
        admin = new AdminVendingMachineFacadeImpl(machine);
    }

    @Test
    public void testFeedingMachineWithCoins() {
        int insertedNoCoins = 3;
        admin.feedWithCoinEachType(insertedNoCoins);

        assertThat(machine.coins().size()).isEqualTo(CoinDenomination.values().length);
        for (CoinDenomination cd : CoinDenomination.values()) {
            assertThat(machine.coins().get(cd)).isEqualTo(insertedNoCoins);
        }
    }

    @Test
    public void testFeedingMachineWithProducts() {
        Product p = new Liquid.Builder()
            .type(LiquidType.COKE)
            .price(BigDecimal.valueOf(2.5))
            .capacity(0.25)
            .build();

        Map<Integer, Product> products = ImmutableMap.<Integer, Product>builder()
            .put(0, p)
            .put(1, p)
            .build();
        admin.feedWithProducts(products);

        List<Product> firstShelve = machine.shelves().get(0);
        assertFalse(firstShelve.isEmpty());
        assertThat(firstShelve.get(0)).isEqualTo(p);
    }

}
