package tdd.vendingMachine;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;
import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Collection of tests for vending machine transaction.
 *
 * @author Łukasz Gadawski
 */
public class VendingMachineTransactionTest {

    private VendingMachine vendingMachine;

    private int testedShelve;

    @Before
    public void init() throws MaximumCoinCapacityExceedException {
        VendingMachineConfig vendingMachineConfig = new VendingMachineConfig.Builder()
            .setMaxCoinNumberOfEachTypeInVendingMachine(100)
            .setNumberOfShelves(5)
            .setMaxProductsOnShelve(4)
            .build();
        vendingMachine = new VendingMachine(vendingMachineConfig);

        vendingMachine.feedWithCoinsEachType(10);

        testedShelve = vendingMachineConfig.getNumberOfShelves() - 1;
        vendingMachine.clearProductsFromShelves();
        vendingMachine.putRandomProductOnShelve(testedShelve);
    }

    @After
    public void after() {
        vendingMachine.cancel();
    }

    @Test
    public void testOpeningTransaction() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE);
        assertThat(t).isNotNull();

        assertThat(t.isOpen()).isEqualTo(true);
        assertThat(CoinDenomination.ONE.getValue()).isEqualTo(t.getInsertedAmount());
        assertThat(productFromShelve.getPrice().subtract(CoinDenomination.ONE.getValue()))
            .isEqualTo(t.getLeftAmountToBuy());
    }

    @Test
    public void testOpeningTransactionAndInsertingMultipleCoins() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Transaction t2 = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Transaction t3 = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        assertThat(t).isNotNull().isEqualTo(t2).isEqualTo(t3);

        assertThat(t.isOpen()).isEqualTo(true);
        BigDecimal insertedCoinsValue = CoinDenomination.ONE_TENTH.getValue().multiply(BigDecimal.valueOf(3));
        assertThat(insertedCoinsValue).isEqualTo(t.getInsertedAmount());
        assertThat(productFromShelve.getPrice().subtract(insertedCoinsValue)).isEqualTo(t.getLeftAmountToBuy());
    }

    @Test
    public void testClosingTransaction() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        Map<CoinDenomination, Integer> coinsBeforeTransaction = Maps.newHashMap(vendingMachine.getCoins());

        int numberOfProductsOnShelve = vendingMachine.getNumberOfProductsOnShelve(testedShelve);

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE);

        assertThat(t).isNotNull();
        assertThat(true).isEqualTo(t.isOpen());
        assertThat(BigDecimal.ZERO).isNotEqualTo(t.getInsertedAmount());
        assertThat(BigDecimal.ZERO).isNotEqualTo(t.getLeftAmountToBuy());
        assertThat(vendingMachine.getCoins()).isNotEqualTo(coinsBeforeTransaction);

        vendingMachine.cancel();

        assertThat(false).isEqualTo(t.isOpen());
        assertThat(vendingMachine.getCoins()).isEqualTo(coinsBeforeTransaction);
        assertThat(t.getCoins().isEmpty());
        assertThat(BigDecimal.ZERO).isEqualTo(t.getInsertedAmount());
        assertThat(BigDecimal.ZERO).isEqualTo(t.getLeftAmountToBuy());
        assertThat(vendingMachine.getNumberOfProductsOnShelve(testedShelve)).isEqualTo(numberOfProductsOnShelve);
    }

    @Test
    public void testClosingTransactionAfterInsertingMultipleCoins() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        Map<CoinDenomination, Integer> coinsBeforeTransaction = Maps.newHashMap(vendingMachine.getCoins());

        int numberOfProductsOnShelve = vendingMachine.getNumberOfProductsOnShelve(testedShelve);

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Transaction t2 = vendingMachine.insertCoin(CoinDenomination.ONE_FIFTH);
        Transaction t3 = vendingMachine.insertCoin(CoinDenomination.ONE_FIFTH);
        Transaction t4 = vendingMachine.insertCoin(CoinDenomination.HALF);

        assertThat(t).isNotNull().isEqualTo(t2).isEqualTo(t3).isEqualTo(t4);
        assertThat(true).isEqualTo(t.isOpen());
        BigDecimal insertedCoinValue =
            CoinDenomination.ONE_TENTH.getValue().add(
            CoinDenomination.ONE_FIFTH.getValue().add(
            CoinDenomination.ONE_FIFTH.getValue().add(
            CoinDenomination.HALF.getValue())));
        assertThat(insertedCoinValue).isEqualTo(t.getInsertedAmount());

        assertThat(productFromShelve.getPrice().subtract(insertedCoinValue)).isEqualTo(t.getLeftAmountToBuy());
        assertThat(vendingMachine.getCoins()).isNotEqualTo(coinsBeforeTransaction);

        vendingMachine.cancel();

        assertThat(false).isEqualTo(t.isOpen());
        assertThat(vendingMachine.getCoins()).isEqualTo(coinsBeforeTransaction);
        assertThat(t.getCoins().isEmpty());
        assertThat(BigDecimal.ZERO).isEqualTo(t.getInsertedAmount());
        assertThat(BigDecimal.ZERO).isEqualTo(t.getLeftAmountToBuy());
        assertThat(vendingMachine.getNumberOfProductsOnShelve(testedShelve)).isEqualTo(numberOfProductsOnShelve);
    }

    @Test
    public void testBuyingProduct() {
        // TODO
    }
}
