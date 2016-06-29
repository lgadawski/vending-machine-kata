package tdd.vendingMachine;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.display.DisplayMessages;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;

/**
 * Collection of basic tests of vending machine.
 *
 * @author Łukasz Gadawski
 */
public class VendingMachineTest {

    private VendingMachine vendingMachine;
    private VendingMachineConfig vendingMachineConfig;

    @Before
    public void init() throws MaximumCoinCapacityExceedException {
        vendingMachineConfig =
            new VendingMachineConfig.Builder()
                .setMaxCoinNumberOfEachTypeInVendingMachine(100)
                .setNumberOfShelves(5)
                .setMaxProductsOnShelve(4)
                .build();
        vendingMachine = new VendingMachine(vendingMachineConfig);

        vendingMachine.feedWithCoinsEachType(10);
    }

    @Test
    public void testSelectingEmptyShelve() {
        vendingMachine.clearProductsFromShelves();
        vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 1);

        Assertions.assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
        Assertions.assertThat(vendingMachine.getDisplayMessage()).isEqualTo(DisplayMessages.NO_PRODUCTS_ON_SHELVE);
    }

    @Test
    public void testSelectingShelveNumberOutOfShelveNumbers() {
        vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() + 1);

        Assertions.assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
        Assertions.assertThat(vendingMachine.getDisplayMessage())
            .isEqualTo(DisplayMessages.SELECTED_SHELVE_NO_OUT_OF_POSSIBLE_SHELVE_NUMBERS);
    }

    @Test
    public void testSelectingNonEmptyShelve() {
        vendingMachine.putRandomProductsOnShelves();
        vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 1);

        Assertions.assertThat(vendingMachine.getSelectedShelveNumber())
            .isEqualTo(vendingMachineConfig.getNumberOfShelves() - 1);
    }

}
