package tdd.vendingMachine;

import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.display.DisplayMessages;
import tdd.vendingMachine.products.Product;
import tdd.vendingMachine.products.liquid.Liquid;
import tdd.vendingMachine.products.liquid.LiquidType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Collection of basic tests of vending machine.
 *
 * @author Łukasz Gadawski
 */
public class VendingMachineTest {

    private VendingMachine vendingMachine;
    private VendingMachineConfig vendingMachineConfig;
    private ResourceBundle bundle;

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

        vendingMachine.feedWithCoinsEachType(10);

        Product p1 = new Liquid.Builder()
            .type(LiquidType.COKE)
            .price(BigDecimal.valueOf(2.5))
            .capacity(0.25)
            .build();
        vendingMachine.putRandomProductsOnShelves(Collections.singletonList(p1));

        bundle = ResourceBundle.getBundle(vendingMachineConfig.getBundle());
    }

    @Test
    public void testSelectingEmptyShelve() {
        vendingMachine.clearProductsFromShelves();
        vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 1);

        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
        assertThat(vendingMachine.getDisplayMessage())
            .isEqualTo(bundle.getString(DisplayMessages.NO_PRODUCTS_ON_SHELVE));
        assertTrue(vendingMachine.getReturnedChange().isEmpty());
        assertThat(vendingMachine.getReturnedProduct()).isNull();
    }

    @Test
    public void testSelectingShelveNumberOutOfShelveNumbers() {
        vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() + 1);

        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
        assertThat(vendingMachine.getDisplayMessage())
            .isEqualTo(bundle.getString(DisplayMessages.SELECTED_SHELVE_NO_OUT_OF_POSSIBLE_SHELVE_NUMBERS));
        assertTrue(vendingMachine.getReturnedChange().isEmpty());
        assertThat(vendingMachine.getReturnedProduct()).isNull();
    }

    @Test
    public void testSelectingNonEmptyShelve() {
        vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 1);

        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(vendingMachineConfig.getNumberOfShelves() - 1);
        assertTrue(vendingMachine.getReturnedChange().isEmpty());
        assertThat(vendingMachine.getReturnedProduct()).isNull();
    }

}
