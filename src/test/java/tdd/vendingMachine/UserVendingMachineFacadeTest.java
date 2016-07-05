package tdd.vendingMachine;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.facade.admin.AdminVendingMachineFacade;
import tdd.vendingMachine.facade.admin.AdminVendingMachineFacadeImpl;
import tdd.vendingMachine.facade.user.UserVendingMachineFacade;
import tdd.vendingMachine.facade.user.UserVendingMachineFacadeImpl;
import tdd.vendingMachine.products.Product;
import tdd.vendingMachine.products.liquid.Liquid;
import tdd.vendingMachine.products.liquid.LiquidType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for user vending machine facade.
 *
 * @author Łukasz Gadawski
 */
public class UserVendingMachineFacadeTest {

    private VendingMachineConfig config;
    private UserVendingMachineFacade user;
    private AdminVendingMachineFacade admin;

    @Before
    public void init() {
        config = new VendingMachineConfig.Builder()
            .setMaxCoinNumberOfEachTypeInVendingMachine(10)
            .setNumberOfShelves(1)
            .setMaxProductsOnShelve(4)
            .setBundle("i18n.messages")
            .build();

        VendingMachine vm = new VendingMachine(config);
        user = new UserVendingMachineFacadeImpl(vm);
        admin = new AdminVendingMachineFacadeImpl(vm);
    }

    @After
    public void after() {
        user.cancel();
    }

    @Test
    public void testSelectShelveNumber() {
        int shelveNumber = config.getNumberOfShelves() - 1;
        user.selectShelveNumber(shelveNumber);

        assertThat(user.getSelectedShelveNumber()).isEqualTo(-1);
    }

    @Test
    public void testGetDisplayMessage() {
        assertThat(user.getDisplayMessage()).isNotNull();
    }

    @Test
    public void testGetSelectedShelveNumber() {
        assertThat(user.getSelectedShelveNumber()).isEqualTo(-1);
    }

    @Test
    public void testInsertCoin() {
        user.insertCoin(CoinDenomination.HALF);

        // wasn't selected any shelve
        assertThat(user.getReturnedProduct()).isNull();
    }

    @Test
    public void testCancel() {
        user.insertCoin(CoinDenomination.HALF);
        user.cancel();

        assertThat(user.getReturnedChange().get(CoinDenomination.HALF)).isNotNull();
    }

    @Test
    public void testShelves() {
        Map<Integer, List<Product>> shelves = user.getShelves();

        // not inserted any products yet
        assertTrue(shelves.get(config.getNumberOfShelves() - 1).isEmpty());
    }

    @Test
    public void testGetReturnedProductAndChange() {
        admin.feedWithCoinEachType(5);
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

        CoinDenomination inserted = CoinDenomination.FIVE;
        user.selectShelveNumber(config.getNumberOfShelves() - 1);
        user.insertCoin(inserted);

        assertThat(user.getReturnedProduct()).isNotNull();
        assertThat(user.getReturnedProduct()).isEqualTo(p);

        assertFalse(user.getReturnedChange().isEmpty());
        assertThat(CoinDenomination.ValueCounter.count(user.getReturnedChange()))
            .isEqualTo(inserted.getValue().subtract(p.getPrice()));
    }

}
