package tdd.vendingMachine;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents transaction instance.
 *
 * @author Łukasz Gadawski
 */
class Transaction {

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

    /**
     * @return true if inserted amount of coins is enough to buy product in transaction, false otherwise
     */
    boolean insertCoin(CoinDenomination cd) {
        coins.put(cd, coins.getOrDefault(cd, 0) + 1);

        insertedAmount = insertedAmount.add(cd.getValue());
        leftAmountToBuy = leftAmountToBuy.subtract(cd.getValue());

        return leftAmountToBuy.compareTo(BigDecimal.ZERO) <= 0;

    }

    BigDecimal getInsertedAmount() {
        return insertedAmount;
    }

    /**
     * @return left amount to buy. If enough money has been inserted method should return value <= 0.
     * 0 means that exact amount of money has been put.
     */
    BigDecimal getLeftAmountToBuy() {
        return leftAmountToBuy;
    }

    protected Map<CoinDenomination, Integer> coins() {
        return ImmutableMap.copyOf(coins);
    }

    public Product getProduct() {
        return product;
    }

}
