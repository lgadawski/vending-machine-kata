package tdd.vendingMachine;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;
import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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

        BigDecimal price = BigDecimal.valueOf(2.5);
        vendingMachine.putRandomProductOnShelve(testedShelve, price);
    }

    @After
    public void after() {
        vendingMachine.cancel();
    }

    @Test
    public void testOpeningTransaction() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        vendingMachine.insertCoin(CoinDenomination.ONE);
        Transaction t = vendingMachine.transaction();
        assertThat(t).isNotNull();

        assertThat(t.isOpen()).isEqualTo(true);
        assertTrue(t.getInsertedAmount().compareTo(CoinDenomination.ONE.getValue()) == 0);
        assertTrue(t.getLeftAmountToBuy()
            .compareTo(productFromShelve.getPrice().subtract(CoinDenomination.ONE.getValue())) == 0);
    }

    @Test
    public void testOpeningTransactionAndInsertingMultipleCoins() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        Transaction t = vendingMachine.transaction();
        assertThat(t).isNotNull();

        assertThat(t.isOpen()).isEqualTo(true);
        BigDecimal insertedCoinsValue = CoinDenomination.ONE_TENTH.getValue().multiply(BigDecimal.valueOf(3));
        assertTrue(t.getInsertedAmount().compareTo(insertedCoinsValue) == 0);
        assertTrue(t.getLeftAmountToBuy().compareTo(productFromShelve.getPrice().subtract(insertedCoinsValue)) == 0);
    }

    @Test
    public void testClosingTransaction() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        Map<CoinDenomination, Integer> coinsBeforeTransaction = Maps.newHashMap(vendingMachine.coins());

        int numberOfProductsOnShelve = vendingMachine.getNumberOfProductsOnShelve(testedShelve);

        Transaction t = vendingMachine.transaction();
        vendingMachine.insertCoin(CoinDenomination.ONE);

        assertThat(t).isNotNull();
        assertThat(t.isOpen()).isEqualTo(true);
        assertTrue(BigDecimal.ZERO.compareTo(t.getInsertedAmount()) < 0);
        assertTrue(BigDecimal.ZERO.compareTo(t.getLeftAmountToBuy()) < 0);
        assertThat(vendingMachine.coins()).isNotEqualTo(coinsBeforeTransaction);

        vendingMachine.cancel();

        assertThat(t.isOpen()).isEqualTo(false);
        assertThat(vendingMachine.coins()).isEqualTo(coinsBeforeTransaction);
        assertThat(t.coins().isEmpty());
        assertTrue(BigDecimal.ZERO.compareTo(t.getInsertedAmount()) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(t.getLeftAmountToBuy()) == 0);
        assertThat(vendingMachine.getNumberOfProductsOnShelve(testedShelve)).isEqualTo(numberOfProductsOnShelve);
        assertThat(vendingMachine.getReturnedProduct()).isNull();
    }

    @Test
    public void testClosingTransactionAfterInsertingMultipleCoins() {
        Product productFromShelve = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(productFromShelve).isNotNull();

        Map<CoinDenomination, Integer> coinsBeforeTransaction = Maps.newHashMap(vendingMachine.coins());

        int numberOfProductsOnShelve = vendingMachine.getNumberOfProductsOnShelve(testedShelve);

        vendingMachine.insertCoin(CoinDenomination.ONE_FIFTH);
        vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);
        vendingMachine.insertCoin(CoinDenomination.HALF);
        vendingMachine.insertCoin(CoinDenomination.ONE_FIFTH);

        Transaction tx = vendingMachine.transaction();
        assertThat(tx).isNotNull();
        assertThat(tx.isOpen()).isEqualTo(true);
        BigDecimal insertedCoinValue =
            CoinDenomination.ONE_TENTH.getValue().add(
            CoinDenomination.ONE_FIFTH.getValue().add(
            CoinDenomination.ONE_FIFTH.getValue().add(
            CoinDenomination.HALF.getValue())));
        assertTrue(tx.getInsertedAmount().compareTo(insertedCoinValue) == 0);

        assertTrue(tx.getLeftAmountToBuy().compareTo(productFromShelve.getPrice().subtract(insertedCoinValue)) == 0);
        assertThat(vendingMachine.coins()).isNotEqualTo(coinsBeforeTransaction);
        assertThat(vendingMachine.getReturnedProduct()).isNull();

        vendingMachine.cancel();

        assertThat(tx.isOpen()).isEqualTo(false);
        assertThat(vendingMachine.coins()).isEqualTo(coinsBeforeTransaction);
        assertThat(tx.coins().isEmpty());
        assertTrue(BigDecimal.ZERO.compareTo(tx.getInsertedAmount()) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(tx.getLeftAmountToBuy()) == 0);
        assertThat(vendingMachine.getNumberOfProductsOnShelve(testedShelve)).isEqualTo(numberOfProductsOnShelve);
        assertThat(vendingMachine.getReturnedProduct()).isNull();
    }

    @Test
    public void testBuyingProductNoChange() {
        Product product = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(product).isNotNull();

        assertThat(vendingMachine.getReturnedProduct()).isNull();

        BigDecimal insertedVal = BigDecimal.ZERO;
        for (CoinDenomination cd : Arrays.asList(CoinDenomination.TWO, CoinDenomination.HALF)) {
            vendingMachine.insertCoin(cd);
            insertedVal = insertedVal.add(cd.getValue());
        }

        assertThat(vendingMachine.getReturnedProduct()).isEqualTo(product);
        assertThat(vendingMachine.getReturnedChange().isEmpty()).isEqualTo(true);
    }

    @Test
    public void testBuyingProductSingleCoinChange() {
        Product product = vendingMachine.selectShelveNumber(testedShelve);
        assertThat(product).isNotNull();

        assertThat(vendingMachine.getReturnedProduct()).isNull();

        List<CoinDenomination> cdToInsert = Arrays.asList(CoinDenomination.TWO, CoinDenomination.ONE);
        BigDecimal insertedVal = BigDecimal.ZERO;
        for (CoinDenomination cd : cdToInsert) {
            vendingMachine.insertCoin(cd);
            insertedVal = insertedVal.add(cd.getValue());
        }

        assertThat(vendingMachine.getReturnedProduct()).isEqualTo(product);

        assertThat(vendingMachine.getReturnedChange().isEmpty()).isEqualTo(false);
        assertTrue(vendingMachine.getReturnedChangeValue().compareTo(insertedVal.subtract(product.getPrice())) == 0);
    }
}

