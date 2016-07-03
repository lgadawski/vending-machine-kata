package tdd.vendingMachine;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import tdd.vendingMachine.changeAlgorithm.CoinReturningAlgorithm;
import tdd.vendingMachine.changeAlgorithm.NotEnoughCoinsToReturnException;
import tdd.vendingMachine.display.Display;
import tdd.vendingMachine.display.DisplayMessages;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;
import tdd.vendingMachine.products.Product;
import tdd.vendingMachine.products.liquid.Liquid;
import tdd.vendingMachine.products.liquid.LiquidType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Represents vending machine.
 *
 * @author Łukasz Gadawski
 */
public class VendingMachine {

    private final VendingMachineConfig config;

    private final Transaction tx;

    private final Map<Integer, List<Product>> shelves;

    /** Contains product returned after transaction */
    private Product returnedProduct;

    /** Map of coins inserted into machine. Maps is sorted that higher value denomination are first. */
    private final Map<CoinDenomination, Integer> coins;

    /** Contains coins returned after transaction */
    private Map<CoinDenomination, Integer> returnedChange;

    private final Display display;

    private int selectedShelveNumber = -1;


    public VendingMachine(VendingMachineConfig config) {
        Preconditions.checkNotNull(config);

        this.shelves = Maps.newHashMapWithExpectedSize(config.getNumberOfShelves());
        for (int i = 0; i < config.getNumberOfShelves(); ++i) {
            this.shelves.put(i, Lists.newArrayListWithCapacity(config.getMaxProductsOnShelve()));
        }

        // coins will be sorted in descending order by coin denomination value
        this.coins = Maps.newTreeMap(CoinDenomination.valueDescendingComparator);
        for (CoinDenomination cd : CoinDenomination.values()) {
            this.coins.put(cd, 0);
        }

        this.returnedChange = Maps.newHashMap();

        this.display = new Display(DisplayMessages.HELLO_MESSAGE);

        this.tx = new Transaction();

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

    protected void putRandomProductOnShelve(int shelveNo, BigDecimal price) {
        // TODO correct to randomizing product on shelves
        shelves.replace(shelveNo, Lists.newArrayList(new Liquid(LiquidType.COKE, price, 0.33)));
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
     * @param cd inserted coin denomination
     */
    public void insertCoin(CoinDenomination cd) {
        Preconditions.checkState(getSelectedShelveNumber() != -1);

        if (!tx.isOpen()) {
            tx.open();
            Product product = getProductFromSelectedShelve();
            if (product == null) {
                tx.close();
                display.setMessage(DisplayMessages.NO_PRODUCTS_ON_SHELVE);
            }
            tx.setProduct(product);
        }

        try {
            putCoinIntoMachine(cd, 1);
        } catch (MaximumCoinCapacityExceedException e) {
            beforeTransactionCancelClose(tx);
            tx.close();
        }

        if (tx.insertCoin(cd)) {
            returnChange(tx.getLeftAmountToBuy());
            returnProduct(tx);
            tx.close();
        }
        display.setMessage(String.valueOf(tx.getLeftAmountToBuy()));
    }

    private void returnChange(BigDecimal leftAmountToBuy) {
        Map<CoinDenomination, Integer> change = null;
        try {
            change = new CoinReturningAlgorithm(coins()).getChange(leftAmountToBuy.negate());
        } catch (NotEnoughCoinsToReturnException e) {
            // TODO handle exception
            e.printStackTrace();
        }
        removeCoinsFromMachine(change);
        returnedChange = change;
    }

    private void beforeTransactionCancelClose(Transaction t) {
        returnProductOnShelve(t.getProduct());
        removeCoinsFromMachine(t.coins());
        returnedChange = t.coins();
    }

    private void returnProduct(Transaction t) {
        this.returnedProduct = t.getProduct();
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

        if (tx.isOpen()) {
            insertedCoins = Maps.newHashMap(tx.coins());
            beforeTransactionCancelClose(tx);
            tx.close();
            display.setMessage(DisplayMessages.HELLO_MESSAGE);
        }

        return insertedCoins;
    }

    private void removeCoinsFromMachine(Map<CoinDenomination, Integer> insertedCoins) {
        Preconditions.checkNotNull(insertedCoins);

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

    public Map<CoinDenomination, Integer> coins() {
        return ImmutableMap.copyOf(coins);
    }

    public int getNumberOfProductsOnShelve(int shelveNo) {
        return shelves.get(shelveNo).size();
    }

    public Product getReturnedProduct() {
        return returnedProduct;
    }

    public Map<CoinDenomination, Integer> getReturnedChange() {
        return returnedChange;
    }

    public BigDecimal getReturnedChangeValue() {
        return CoinDenomination.ValueCounter.count(getReturnedChange());
    }

    protected Transaction transaction() {
        return tx;
    }
}
