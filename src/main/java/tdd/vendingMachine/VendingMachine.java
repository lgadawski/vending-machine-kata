package tdd.vendingMachine;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import tdd.vendingMachine.display.Display;
import tdd.vendingMachine.display.DisplayMessages;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;
import tdd.vendingMachine.products.liquid.Liquid;
import tdd.vendingMachine.products.liquid.LiquidType;
import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Represents vending machine.
 *
 * @author Łukasz Gadawski
 */
public final class VendingMachine {

    private final VendingMachineConfig config;

    private final Map<Integer, List<Product>> shelves;

    private final Map<CoinDenomination, Integer> coins;

    private final Display display;

    private int selectedShelveNumber = -1;


    public VendingMachine(VendingMachineConfig config) {
        Preconditions.checkNotNull(config);

        this.shelves = Maps.newHashMapWithExpectedSize(config.getNumberOfShelves());
        for (int i = 0; i < config.getNumberOfShelves(); ++i) {
            this.shelves.put(i, Lists.newArrayListWithCapacity(config.getMaxProductsOnShelve()));
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
    public void feedWithCoinsEachType(int coinNumber) throws MaximumCoinCapacityExceedException {
        for (Map.Entry<CoinDenomination, Integer> entry : coins.entrySet()) {
            putCoinIntoMachine(entry.getKey(), entry.getValue(), coinNumber);
        }
    }

    private void putCoinIntoMachine(CoinDenomination cd, Integer cdCurrentCount, int coinNumber)
        throws MaximumCoinCapacityExceedException {
        if (cdCurrentCount + coinNumber > config.getMaxCoinNumberOfEachTypeInVendingMachine()) {
            display.setMessage(DisplayMessages.MAX_MACHINE_COIN_CAPACITY_REACHED);
            throw new MaximumCoinCapacityExceedException();
        }

        coins.replace(cd, cdCurrentCount + coinNumber);
    }

    private void putCoinIntoMachine(CoinDenomination cd, int coinNumber) throws MaximumCoinCapacityExceedException {
        putCoinIntoMachine(cd, coins.get(cd), coinNumber);
    }

    protected void putRandomProductsOnShelves() {
        // TODO correct to randomizing product on shelves
        shelves.entrySet().stream()
            .forEach(entry -> {
                for (int i = 0; i < config.getMaxProductsOnShelve(); i++) {
                    entry.getValue().add(new Liquid(LiquidType.COKE, BigDecimal.valueOf(2.5), 0.33));
                }
            });
    }

    protected void putRandomProductOnShelve(int shelveNo) {
        shelves.replace(shelveNo, Lists.newArrayList(new Liquid(LiquidType.COKE, BigDecimal.valueOf(2.5), 0.33)));
    }

    public Product selectShelveNumber(int selectedShelveNumber) {
        List<Product> productFromShelve = shelves.get(selectedShelveNumber);
        if (productFromShelve == null) {
            display.setMessage(DisplayMessages.SELECTED_SHELVE_NO_OUT_OF_POSSIBLE_SHELVE_NUMBERS);
            setSelectedShelveNumber(-1);
            return null;
        }
        if (productFromShelve.isEmpty()) {
            display.setMessage(DisplayMessages.NO_PRODUCTS_ON_SHELVE);
            setSelectedShelveNumber(-1);
            return null;
        }

        Product product = productFromShelve.get(0);
        display.setMessage(String.valueOf(product.getPrice()));
        setSelectedShelveNumber(selectedShelveNumber);

        return product;
    }

    public String getDisplayMessage() {
        return display.getMessage();
    }

    private void setSelectedShelveNumber(int selectedShelveNumber) {
        this.selectedShelveNumber = selectedShelveNumber;
    }

    public int getSelectedShelveNumber() {
        return selectedShelveNumber;
    }

    protected void clearProductsFromShelves() {
        for (int i = 0; i < config.getNumberOfShelves(); i++) {
            shelves.get(i).clear();
        }
    }

    /**
     * Inserting coin opening transaction. It assumes that before inserting coin user has selected shelves to get
     * product from, otherwise there is no possibility to insert coin into vending machine.
     *
     * @param cd inserted coind denomination
     * @return opened transaction, or current transaction if it is already open
     */
    public Transaction insertCoin(CoinDenomination cd) {
        Preconditions.checkState(getSelectedShelveNumber() != -1);

        Transaction t = Transaction.INSTANCE;
        if (!t.isOpen()) {
            t.open();
            Product product = getProductFromSelectedShelve();
            if (product == null) {
                t.close();
                display.setMessage(DisplayMessages.NO_PRODUCTS_ON_SHELVE);
                return t;
            }
            t.setProduct(product);
        }

        try {
            putCoinIntoMachine(cd, 1);
        } catch (MaximumCoinCapacityExceedException e) {
            beforeTransactionClose(t);
            t.close();

            return t;
        }

        t.insertCoin(cd);
        display.setMessage(String.valueOf(t.getLeftAmountToBuy()));

        return t;
    }

    private void beforeTransactionClose(Transaction t) {
        returnProductOnShelve(t.getProduct());
        returnInsertedCoins(t.getCoins());
    }

    /**
     * Canceling current transaction if there is any open. If so it cancel transaction, removes inserted coins
     * from vending machine and return map that represents inserted coins. Canceling transaction is possible
     * if there was selected shelve to get product from.
     *
     * @return map contains inserted coins during last transaction. Empty map if there wasn't open transaction.
     */
    public Map<CoinDenomination, Integer> cancel() {
        Preconditions.checkState(getSelectedShelveNumber() != -1);

        Map<CoinDenomination, Integer> insertedCoins = Collections.emptyMap();

        Transaction t = Transaction.INSTANCE;
        if (t.isOpen()) {
            insertedCoins = Maps.newHashMap(t.getCoins());
            beforeTransactionClose(t);
            t.close();
            display.setMessage(DisplayMessages.HELLO_MESSAGE);
        }

        return insertedCoins;
    }

    private void returnInsertedCoins(Map<CoinDenomination, Integer> insertedCoins) {
        coins.entrySet().stream()
            .forEach(entry -> {
                Integer insertedNumber = insertedCoins.get(entry.getKey());
                if (insertedNumber != null) {
                    coins.replace(entry.getKey(), entry.getValue() - insertedNumber);
                }
            });
    }

    private void returnProductOnShelve(Product product) {
        Preconditions.checkNotNull(product);

        shelves.get(getSelectedShelveNumber()).add(product);
    }

    private Product getProductFromSelectedShelve() {
        List<Product> products = shelves.get(getSelectedShelveNumber());
        if (products.isEmpty()) {
            return null;
        }

        return  products.remove(0);
    }

    public Map<CoinDenomination, Integer> getCoins() {
        return coins;
    }

    public int getNumberOfProductsOnShelve(int shelveNo) {
        return shelves.get(shelveNo).size();
    }
}
