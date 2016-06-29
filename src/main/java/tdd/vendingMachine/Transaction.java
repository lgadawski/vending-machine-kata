package tdd.vendingMachine;

import com.google.common.collect.Maps;
import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents transaction instance.
 *
 * @author Łukasz Gadawski
 */
enum Transaction {
    INSTANCE;

    private boolean isOpen;

    private Product product;

    private Map<CoinDenomination, Integer> coins;

    private BigDecimal insertedAmount;

    private BigDecimal leftAmountToBuy;


    Transaction() {
        coins = Maps.newHashMap();
    }

    void open() {
        isOpen = true;
        insertedAmount = BigDecimal.ZERO;
        leftAmountToBuy = BigDecimal.ZERO;
    }

    void close() {
        isOpen = false;
        product = null;
        coins.clear();
        insertedAmount = BigDecimal.ZERO;
        leftAmountToBuy = BigDecimal.ZERO;
    }

    boolean isOpen() {
        return isOpen;
    }

    protected void setProduct(Product product) {
        this.product = product;
        this.leftAmountToBuy = product.getPrice();
    }

    void insertCoin(CoinDenomination cd) {
        Integer currentCoinNumber = coins.get(cd);
        if (currentCoinNumber == null) {
            coins.put(cd, 1);
        } else {
            coins.replace(cd, ++currentCoinNumber);
        }

        insertedAmount = insertedAmount.add(cd.getValue());
        leftAmountToBuy = leftAmountToBuy.subtract(cd.getValue());
    }

    BigDecimal getInsertedAmount() {
        return insertedAmount;
    }

    BigDecimal getLeftAmountToBuy() {
        return leftAmountToBuy;
    }

    protected Map<CoinDenomination, Integer> getCoins() {
        return coins;
    }

    public Product getProduct() {
        return product;
    }
}
