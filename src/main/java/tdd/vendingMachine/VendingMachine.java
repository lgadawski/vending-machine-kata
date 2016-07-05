package tdd.vendingMachine;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.SerializationUtils;
import tdd.vendingMachine.changeAlgorithm.CoinReturningAlgorithm;
import tdd.vendingMachine.changeAlgorithm.NotEnoughCoinsToReturnException;
import tdd.vendingMachine.display.Display;
import tdd.vendingMachine.display.DisplayMessages;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;
import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;
import java.util.*;


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

    private ResourceBundle bundle;


    public VendingMachine(VendingMachineConfig config) {
        Preconditions.checkNotNull(config);

        this.config = config;

        this.shelves = Maps.newHashMapWithExpectedSize(config.getNumberOfShelves());
        for (int i = 0; i < config.getNumberOfShelves(); ++i) {
            this.shelves.put(i, Lists.newLinkedList());
        }

        // coins will be sorted in descending order by coin denomination value
        this.coins = Maps.newTreeMap(CoinDenomination.valueDescendingComparator);
        for (CoinDenomination cd : CoinDenomination.values()) {
            this.coins.put(cd, 0);
        }

        this.returnedChange = Maps.newHashMap();

        this.tx = new Transaction();

        this.bundle = ResourceBundle.getBundle(config.getBundle());

        this.display = new Display();
        setDisplayMessage(DisplayMessages.HELLO_MESSAGE);
    }

    protected void reset() {
        setDisplayMessage(DisplayMessages.HELLO_MESSAGE);
        this.shelves.clear();
        this.coins.clear();
        this.returnedChange.clear();
        this.returnedProduct = null;
        this.tx.close();
    }

    /**
     * Feeds vending machine with coinNumber of each denomination.
     *
     * @param coinNumber number of coins of each type inserted into vending machine.
     */
    public void feedWithCoinsEachType(int coinNumber) {
        for (Map.Entry<CoinDenomination, Integer> entry : coins.entrySet()) {
            putCoinIntoMachine(entry.getKey(), entry.getValue(), coinNumber);
        }
    }

    private void putCoinIntoMachine(CoinDenomination cd, Integer cdCurrentCount, int coinNumber) {
        if (cdCurrentCount + coinNumber > config.getMaxCoinNumberOfEachTypeInVendingMachine()) {
            setDisplayMessage(DisplayMessages.MAX_MACHINE_COIN_CAPACITY_REACHED);
            throw new MaximumCoinCapacityExceedException();
        }

        coins.put(cd, cdCurrentCount + coinNumber);
    }

    private void putCoinIntoMachine(CoinDenomination cd, int coinNumber) {
        putCoinIntoMachine(cd, coins.get(cd), coinNumber);
    }

    protected void putRandomProductsOnShelves(List<Product> possibleProductList) {
        Random random = new Random();
        shelves.entrySet().stream()
            .forEach(entry -> {
                Product product = possibleProductList.get(random.nextInt(possibleProductList.size()));
                for (int i = 0; i < config.getMaxProductsOnShelve(); i++) {
                    entry.getValue().add(SerializationUtils.clone(product));
                }
            });
    }

    private void putProductOnShelve(Product product) {
        shelves.get(getSelectedShelveNumber()).add(product);
    }

    public Product selectShelveNumber(int selectedShelveNumber) {
        List<Product> productFromShelve = shelves.get(selectedShelveNumber);
        if (productFromShelve == null) {
            setDisplayMessage(DisplayMessages.SELECTED_SHELVE_NO_OUT_OF_POSSIBLE_SHELVE_NUMBERS);
            setSelectedShelveNumber(-1);
            return null;
        }
        if (productFromShelve.isEmpty()) {
            setDisplayMessage(DisplayMessages.NO_PRODUCTS_ON_SHELVE);
            setSelectedShelveNumber(-1);
            return null;
        }

        Product product = productFromShelve.get(0);
        setDisplayMessageExact(String.valueOf(product.getPrice()));
        setSelectedShelveNumber(selectedShelveNumber);

        return product;
    }

    private void resetSelectedShelve() {
        this.selectedShelveNumber = -1;
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
     * Inserting coin opening transaction. If before inserting coin there wasn't selected any shelve coin will
     * be put into returnChange collection.
     *
     * @param cd inserted coin denomination
     */
    public void insertCoin(CoinDenomination cd) {
        if (getSelectedShelveNumber() == -1) {
            putReturnedChange(Collections.singletonMap(cd, 1));
            return;
        }

        if (!tx.isOpen()) {
            tx.open();
            Product product = getProductFromSelectedShelve();
            if (product == null) {
                tx.close();
                setDisplayMessage(DisplayMessages.NO_PRODUCTS_ON_SHELVE);

                return;
            }
            tx.setProduct(product);
        }

        try {
            putCoinIntoMachine(cd, 1);
        } catch (MaximumCoinCapacityExceedException e) {
            beforeTransactionCancelClose(tx);
            tx.close();

            return;
        }

        if (tx.insertCoin(cd)) {
            try {
                returnChange(tx.getLeftAmountToBuy());
            } catch (NotEnoughCoinsToReturnException e) {
                putProductOnShelve(tx.getProduct());
                putReturnedChange(tx.coins());
                resetSelectedShelve();
                tx.close();
                setDisplayMessage(DisplayMessages.NO_COINS_TO_RETURN);

                return;
            }
            returnProduct(tx);
            resetSelectedShelve();
            tx.close();
            setDisplayMessage(DisplayMessages.HELLO_MESSAGE);

            return;
        }
        setDisplayMessageExact(String.valueOf(tx.getLeftAmountToBuy()));
    }

    private void returnChange(BigDecimal leftAmountToBuy) {
        CoinReturningAlgorithm algorithm = new CoinReturningAlgorithm(coins());
        Map<CoinDenomination, Integer> change = algorithm.getChange(leftAmountToBuy.negate());
        removeCoinsFromMachine(change);
        putReturnedChange(change);
    }

    private void beforeTransactionCancelClose(Transaction t) {
        returnProductOnShelve(t.getProduct());
        removeCoinsFromMachine(t.coins());
        putReturnedChange(t.coins());
        resetSelectedShelve();
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
        Map<CoinDenomination, Integer> insertedCoins = Collections.emptyMap();

        if (tx.isOpen()) {
            insertedCoins = Maps.newHashMap(tx.coins());
            beforeTransactionCancelClose(tx);
            tx.close();
            setDisplayMessage(DisplayMessages.HELLO_MESSAGE);
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

        return products.remove(0);
    }

    public Map<CoinDenomination, Integer> coins() {
        return ImmutableMap.copyOf(coins);
    }

    public Map<Integer, List<Product>> shelves() {
        return ImmutableMap.copyOf(shelves);
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

    private void putReturnedChange(Map<CoinDenomination, Integer> returnedChange) {
        for (Map.Entry<CoinDenomination, Integer> entry : returnedChange.entrySet()) {
            CoinDenomination cd = entry.getKey();
            this.returnedChange.put(cd, this.returnedChange.getOrDefault(cd, 0) + entry.getValue());
        }
    }

    public BigDecimal getReturnedChangeValue() {
        return CoinDenomination.ValueCounter.count(getReturnedChange());
    }

    protected Transaction transaction() {
        return tx;
    }

    /**
     * Set display message from bundle by passed property key.
     */
    private void setDisplayMessage(String displayMessageKey) {
        display.setMessage(bundle.getString(displayMessageKey));
    }

    /**
     * Set display message exactly as passed arg.
     */
    private void setDisplayMessageExact(String s) {
        display.setMessage(s);
    }
}
