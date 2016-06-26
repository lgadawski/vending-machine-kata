package tdd.vendingMachine;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import tdd.vendingMachine.display.Display;
import tdd.vendingMachine.display.DisplayMessages;
import tdd.vendingMachine.products.Product;

import java.util.List;
import java.util.Map;


/**
 * Represents vending machine.
 *
 * @author Łukasz Gadawski
 */
public final class VendingMachine {

    private final VendingMachineConfig config;

    private final Map<Integer, List<Product>> shelve;

    private final Map<CoinDenomination, Integer> coins;

    private final Display display;

    private int selectedShelveNumber = -1;

    public VendingMachine(VendingMachineConfig config) {
        Preconditions.checkNotNull(config);

        this.shelve = Maps.newHashMapWithExpectedSize(config.getNumberOfShelves());
        for (int i = 0; i < config.getNumberOfShelves(); ++i) {
            this.shelve.put(i, Lists.newLinkedList());
        }

        this.coins = Maps.newHashMap();
        for (CoinDenomination cd : CoinDenomination.values()) {
            this.coins.put(cd, 0);
        }

        this.display = new Display(DisplayMessages.HELLO_MESSAGE);

        this.config = config;
    }

    /**
     * Feeds vending machine with coinNumber of each denomination.
     *
     * @param coinNumber number of coins of each type inserted into vending machine.
     */
    public void feedWithCoinsEachType(int coinNumber) {
        for (Map.Entry<CoinDenomination, Integer> entry : coins.entrySet()) {
            if (entry.getValue() + coinNumber >= config.getMaxCoinNumberOfEachTypeInVendingMachine()) {
                coins.replace(entry.getKey(), config.getMaxCoinNumberOfEachTypeInVendingMachine());
            } else {
                coins.replace(entry.getKey(), entry.getValue() + coinNumber);
            }
        }
    }

    public void selectShelveNumber(int selectedShelveNumber) {
        List<Product> productFromShelve = shelve.get(selectedShelveNumber);
        if (productFromShelve == null) {
            display.setCurrentMessage(DisplayMessages.SELECTED_SHELVE_NO_OUT_OF_POSSIBLE_SHELVE_NUMBERS);
            setSelectedShelveNumber(-1);
            return;
        }
        if (productFromShelve.isEmpty()) {
            display.setCurrentMessage(DisplayMessages.NO_PRODUCTS_ON_SHELVE);
            setSelectedShelveNumber(-1);
            return;
        }

        display.setCurrentMessage(String.valueOf(productFromShelve.get(0).getPrice()));
        setSelectedShelveNumber(selectedShelveNumber);
    }

    public String getDisplayMessage() {
        return display.getCurrentMessage();
    }

    private void setSelectedShelveNumber(int selectedShelveNumber) {
        this.selectedShelveNumber = selectedShelveNumber;
    }

    public int getSelectedShelveNumber() {
        return selectedShelveNumber;
    }

    protected void removeAllProductsFromShelves() {
        for (int i = 0; i < config.getNumberOfShelves(); i++) {
            shelve.get(i).clear();
        }
    }
}
