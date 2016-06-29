package tdd.vendingMachine;

import com.google.common.collect.Maps;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;
import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;
import java.util.Map;

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
        Assertions.assertThat(productFromShelve).isNotNull();

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE);
        Assertions.assertThat(t).isNotNull();

        Assertions.assertThat(t.isOpen()).isEqualTo(true);
        Assertions.assertThat(CoinDenomination.ONE.getValue()).isEqualTo(t.getInsertedAmount());
        Assertions.assertThat(productFromShelve.getPrice().subtract(CoinDenomination.ONE.getValue()))
            .isEqualTo(t.getLeftAmountToBuy());
    }

    @Test
    public void testOpeningTransactionAndInsertingMultipleCoins() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        Assertions.assertThat(productFromShelve).isNotNull();

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Transaction t2 = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Transaction t3 = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Assertions.assertThat(t).isNotNull().isEqualTo(t2).isEqualTo(t3);

        Assertions.assertThat(t.isOpen()).isEqualTo(true);
        BigDecimal insertedCoinsValue = CoinDenomination.ONE_TENTH.getValue().multiply(BigDecimal.valueOf(3));
        Assertions.assertThat(insertedCoinsValue).isEqualTo(t.getInsertedAmount());
        Assertions.assertThat(productFromShelve.getPrice().subtract(insertedCoinsValue))
            .isEqualTo(t.getLeftAmountToBuy());
    }

    @Test
    public void testClosingTransaction() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        Assertions.assertThat(productFromShelve).isNotNull();

        Map<CoinDenomination, Integer> coinsBeforeTransaction = Maps.newHashMap(vendingMachine.getCoins());

        int numberOfProductsOnShelve = vendingMachine.getNumberOfProductsOnShelve(testedShelve);

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE);

        Assertions.assertThat(t).isNotNull();
        Assertions.assertThat(true).isEqualTo(t.isOpen());
        Assertions.assertThat(BigDecimal.ZERO).isNotEqualTo(t.getInsertedAmount());
        Assertions.assertThat(BigDecimal.ZERO).isNotEqualTo(t.getLeftAmountToBuy());
        Assertions.assertThat(vendingMachine.getCoins()).isNotEqualTo(coinsBeforeTransaction);

        vendingMachine.cancel();

        Assertions.assertThat(false).isEqualTo(t.isOpen());
        Assertions.assertThat(vendingMachine.getCoins()).isEqualTo(coinsBeforeTransaction);
        Assertions.assertThat(t.getCoins().isEmpty());
        Assertions.assertThat(BigDecimal.ZERO).isEqualTo(t.getInsertedAmount());
        Assertions.assertThat(BigDecimal.ZERO).isEqualTo(t.getLeftAmountToBuy());
        Assertions.assertThat(vendingMachine.getNumberOfProductsOnShelve(testedShelve))
            .isEqualTo(numberOfProductsOnShelve);
    }

    @Test
    public void testClosingTransactionAfterInsertingMultipleCoins() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        Assertions.assertThat(productFromShelve).isNotNull();

        Map<CoinDenomination, Integer> coinsBeforeTransaction = Maps.newHashMap(vendingMachine.getCoins());

        int numberOfProductsOnShelve = vendingMachine.getNumberOfProductsOnShelve(testedShelve);

        Transaction t = vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Transaction t2 = vendingMachine.insertCoin(CoinDenomination.ONE_FIFTH);
        Transaction t3 = vendingMachine.insertCoin(CoinDenomination.ONE_FIFTH);
        Transaction t4 = vendingMachine.insertCoin(CoinDenomination.HALF);

        Assertions.assertThat(t).isNotNull().isEqualTo(t2).isEqualTo(t3).isEqualTo(t4);
        Assertions.assertThat(true).isEqualTo(t.isOpen());
        BigDecimal insertedCoinValue =
            CoinDenomination.ONE_TENTH.getValue().add(
            CoinDenomination.ONE_FIFTH.getValue().add(
            CoinDenomination.ONE_FIFTH.getValue().add(
            CoinDenomination.HALF.getValue())));
        Assertions.assertThat(
            insertedCoinValue).isEqualTo(t.getInsertedAmount());

        Assertions.assertThat(productFromShelve.getPrice().subtract(insertedCoinValue))
            .isEqualTo(t.getLeftAmountToBuy());
        Assertions.assertThat(vendingMachine.getCoins()).isNotEqualTo(coinsBeforeTransaction);

        vendingMachine.cancel();

        Assertions.assertThat(false).isEqualTo(t.isOpen());
        Assertions.assertThat(vendingMachine.getCoins()).isEqualTo(coinsBeforeTransaction);
        Assertions.assertThat(t.getCoins().isEmpty());
        Assertions.assertThat(BigDecimal.ZERO).isEqualTo(t.getInsertedAmount());
        Assertions.assertThat(BigDecimal.ZERO).isEqualTo(t.getLeftAmountToBuy());
        Assertions.assertThat(vendingMachine.getNumberOfProductsOnShelve(testedShelve))
            .isEqualTo(numberOfProductsOnShelve);
    }
}
