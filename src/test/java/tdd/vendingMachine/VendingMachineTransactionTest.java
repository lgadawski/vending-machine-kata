package tdd.vendingMachine;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.display.DisplayMessages;
import tdd.vendingMachine.products.Product;
import tdd.vendingMachine.products.liquid.Liquid;
import tdd.vendingMachine.products.liquid.LiquidType;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Collection of tests for vending machine transaction.
 *
 * @author Łukasz Gadawski
 */
public class VendingMachineTransactionTest {

    private VendingMachineConfig vendingMachineConfig;
    private VendingMachine vendingMachine;
    private int testedShelve;
    private Product possibleProduct;
    private ResourceBundle bundle;

    @Before
    public void init() {
        vendingMachineConfig = new VendingMachineConfig.Builder()
            .setMaxCoinNumberOfEachTypeInVendingMachine(100)
            .setNumberOfShelves(5)
            .setMaxProductsOnShelve(4)
            .setBundle("i18n.messages")
            .build();
        vendingMachine = new VendingMachine(vendingMachineConfig);

        vendingMachine.feedWithCoinsEachType(10);

        testedShelve = vendingMachineConfig.getNumberOfShelves() - 1;
        vendingMachine.clearProductsFromShelves();

        possibleProduct = new Liquid.Builder()
            .type(LiquidType.COKE)
            .price(BigDecimal.valueOf(2.5))
            .capacity(0.25)
            .build();
        vendingMachine.putRandomProductsOnShelves(Collections.singletonList(possibleProduct));

        bundle = ResourceBundle.getBundle(vendingMachineConfig.getBundle());
    }

    @After
    public void after() {
        vendingMachine.reset();
    }

    @Test
    public void testNoProductsOnShelve() {
        vendingMachine.clearProductsFromShelves();
        Product product = vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 1);

        vendingMachine.insertCoin(CoinDenomination.ONE_TENTH);

        assertThat(product).isNull();
        assertThat(vendingMachine.getDisplayMessage())
            .isEqualTo(bundle.getString(DisplayMessages.NO_PRODUCTS_ON_SHELVE));
        assertFalse(vendingMachine.transaction().isOpen());
    }

    @Test
    public void testMaximumCoinsLimitExceeded() {
        vendingMachine.clearProductsFromShelves();
        possibleProduct = new Liquid.Builder()
            .type(LiquidType.COKE)
            .price(BigDecimal.valueOf(99999))
            .capacity(0.25)
            .build();
        vendingMachine.putRandomProductsOnShelves(Collections.singletonList(possibleProduct));
        Product product = vendingMachine.selectShelveNumber(vendingMachineConfig.getNumberOfShelves() - 1);
        assertThat(product).isNotNull();

        int noCoinsToInsert = vendingMachineConfig.getMaxCoinNumberOfEachTypeInVendingMachine() + 1;
        CoinDenomination cd = CoinDenomination.ONE_TENTH;
        for (int i = 0; i <= noCoinsToInsert; i++) {
            vendingMachine.insertCoin(cd);
        }

        assertFalse(vendingMachine.transaction().isOpen());
        assertThat(vendingMachine.getReturnedChange().get(cd)).isEqualTo(noCoinsToInsert);
        assertThat(vendingMachine.getReturnedProduct()).isNull();
        assertThat(vendingMachine.getDisplayMessage())
            .isEqualTo(bundle.getString(DisplayMessages.MAX_MACHINE_COIN_CAPACITY_REACHED));
    }

    @Test
    public void testInsertingCoinsWhenAnyShelveSelected() {
        Map<CoinDenomination, Integer> inserted = ImmutableMap.<CoinDenomination, Integer>builder()
            .put(CoinDenomination.HALF, 1)
            .put(CoinDenomination.FIVE, 2)
            .build();

        for (Map.Entry<CoinDenomination, Integer> entry : inserted.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                vendingMachine.insertCoin(entry.getKey());
            }
        }

        assertThat(vendingMachine.getReturnedProduct()).isNull();
        assertFalse(vendingMachine.transaction().isOpen());
        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
        assertThat(vendingMachine.getReturnedChange().size()).isEqualTo(inserted.size());
        assertThat(vendingMachine.getReturnedChange()).isEqualTo(inserted);
        assertThat(vendingMachine.getReturnedChangeValue()).isEqualTo(CoinDenomination.ValueCounter.count(inserted));
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
        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
        assertThat(vendingMachine.getReturnedChange().get(CoinDenomination.ONE)).isEqualTo(1);
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
        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
    }

    @Test
    public void testBuyingProductNotEnoughCoinsInMachine() {
        vendingMachine = new VendingMachine(vendingMachineConfig);
        vendingMachine.feedWithCoinsEachType(0);
        vendingMachine.putRandomProductsOnShelves(Collections.singletonList(possibleProduct));

        vendingMachine.selectShelveNumber(testedShelve);

        CoinDenomination insertedCoin = CoinDenomination.FIVE;
        vendingMachine.insertCoin(insertedCoin);

        assertFalse(vendingMachine.transaction().isOpen());
        assertThat(CoinDenomination.ValueCounter.count(vendingMachine.getReturnedChange()))
            .isEqualTo(insertedCoin.getValue());
        assertThat(vendingMachine.getReturnedChange().size()).isEqualTo(1);
        assertThat(vendingMachine.getReturnedProduct()).isNull();
        assertThat(vendingMachine.getDisplayMessage()).isEqualTo(bundle.getString(DisplayMessages.NO_COINS_TO_RETURN));
        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
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
            assertTrue(vendingMachine.transaction().getLeftAmountToBuy().compareTo(
                product.getPrice().subtract(insertedVal)) == 0);
        }

        assertThat(vendingMachine.getReturnedProduct()).isNotNull();
        assertThat(vendingMachine.getReturnedProduct()).isEqualTo(product);
        assertThat(vendingMachine.getReturnedChange().isEmpty()).isEqualTo(true);
        assertThat(vendingMachine.getDisplayMessage()).isEqualTo(bundle.getString(DisplayMessages.HELLO_MESSAGE));
        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
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
        assertThat(vendingMachine.getReturnedProduct()).isNotNull();
        assertThat(vendingMachine.getReturnedChange().isEmpty()).isEqualTo(false);
        assertTrue(vendingMachine.getReturnedChangeValue().compareTo(insertedVal.subtract(product.getPrice())) == 0);
        assertThat(vendingMachine.getSelectedShelveNumber()).isEqualTo(-1);
    }

    @Test
    public void testBuyingProductMultipleCoinChange() {
        Product product = vendingMachine.selectShelveNumber(testedShelve);
        BigDecimal productPrice = BigDecimal.valueOf(2.5);

        assertTrue(product.getPrice().compareTo(productPrice) == 0);

        Map<CoinDenomination, Integer> coins =
            ImmutableMap.<CoinDenomination, Integer>builder()
                .put(CoinDenomination.ONE_TENTH, 3)
                .put(CoinDenomination.ONE_FIFTH, 8)
                .put(CoinDenomination.FIVE, 1)
                .build();

        BigDecimal insertedVal = BigDecimal.ZERO;
        for (Map.Entry<CoinDenomination, Integer> entry : coins.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                vendingMachine.insertCoin(entry.getKey());
                insertedVal = insertedVal.add(entry.getKey().getValue());
                if (vendingMachine.transaction().isOpen()) {
                    assertThat(vendingMachine.getDisplayMessage())
                        .isEqualTo(String.valueOf(productPrice.subtract(insertedVal)));
                } else {
                    assertThat(vendingMachine.getDisplayMessage())
                        .isEqualTo(bundle.getString(DisplayMessages.HELLO_MESSAGE));
                }
            }
        }

        assertThat(vendingMachine.getReturnedProduct()).isNotNull();
        assertThat(vendingMachine.getDisplayMessage()).isEqualTo(bundle.getString(DisplayMessages.HELLO_MESSAGE));
        assertFalse(vendingMachine.transaction().isOpen());
        assertFalse(vendingMachine.getReturnedChange().isEmpty());
        assertTrue(CoinDenomination.ValueCounter.count(vendingMachine.getReturnedChange())
            .compareTo(insertedVal.subtract(productPrice)) == 0);
    }
}

