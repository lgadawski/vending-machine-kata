package tdd.vendingMachine.changeAlgorithm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import tdd.vendingMachine.CoinDenomination;
import tdd.vendingMachine.exceptions.MaximumCoinCapacityExceedException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Łukasz Gadawski
 */
public class CoinReturningAlgorithmTest {

    private CoinReturningAlgorithm algorithm;

    @Before
    public void init() throws MaximumCoinCapacityExceedException {
        Map<CoinDenomination, Integer> availableCoins = Maps.newHashMap();
        for (CoinDenomination cd : CoinDenomination.values()) {
            availableCoins.put(cd, 5);
        }
        algorithm = new CoinReturningAlgorithm(availableCoins);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inputTestBelowZero() {
        algorithm.getChange(BigDecimal.ONE.negate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChangeIllegalArgument() {
        BigDecimal expected = BigDecimal.valueOf(-21.7);
        algorithm.getChange(expected);
    }

    @Test(expected = NotEnoughCoinsToReturnException.class)
    public void testGetChangeNotEngougCoins() {
        BigDecimal expected = BigDecimal.valueOf(100);
        BigDecimal changeValue = CoinDenomination.ValueCounter.count(algorithm.getChange(expected));

        assertTrue(expected.compareTo(changeValue) == 0);
    }

    @Test(expected = NotEnoughCoinsToReturnException.class)
    public void testEmptyCoinsCollection() {
        algorithm = new CoinReturningAlgorithm(Collections.emptyMap());

        assertThat(algorithm.getChange(BigDecimal.ONE));
    }

    @Test(expected = NotEnoughCoinsToReturnException.class)
    public void testNotEnoughCoins() {
        Map<CoinDenomination, Integer> map =
            ImmutableMap.<CoinDenomination, Integer>builder()
                .put(CoinDenomination.ONE_FIFTH, 1)
                .build();

        algorithm = new CoinReturningAlgorithm(map);
        assertThat(algorithm.getChange(BigDecimal.ONE));
    }

    @Test(expected = NotEnoughCoinsToReturnException.class)
    public void testGetChangeException() {
        Map<CoinDenomination, Integer> map =
            ImmutableMap.<CoinDenomination, Integer>builder()
                .put(CoinDenomination.ONE_FIFTH, 5)
                .build();

        algorithm = new CoinReturningAlgorithm(map);

        algorithm.getChange(BigDecimal.valueOf(0.5));
    }

    @Test
    public void testGetChange() {
        Map<CoinDenomination, Integer> map =
            ImmutableMap.<CoinDenomination, Integer>builder()
                .put(CoinDenomination.ONE_FIFTH, 5)
                .build();

        algorithm = new CoinReturningAlgorithm(map);

        BigDecimal changeValue = CoinDenomination.ValueCounter.count(algorithm.getChange(BigDecimal.ONE));
        assertTrue(CoinDenomination.ValueCounter.count(map).compareTo(changeValue) == 0);
    }

    @Test
    public void testGetChangeMultipleCoins() {
        BigDecimal expected = BigDecimal.valueOf(21.7);
        BigDecimal changeValue = CoinDenomination.ValueCounter.count(algorithm.getChange(expected));

        assertTrue(expected.compareTo(changeValue) == 0);
    }

    @Test
    public void testGetChangeMultipleCoins2() {
        BigDecimal expected = BigDecimal.valueOf(19.9);
        BigDecimal changeValue = CoinDenomination.ValueCounter.count(algorithm.getChange(expected));

        assertTrue(expected.compareTo(changeValue) == 0);
    }

    @Test
    public void testGetChangeMultipleCoins3() {
        BigDecimal expected = BigDecimal.valueOf(5.5);
        BigDecimal changeValue = CoinDenomination.ValueCounter.count(algorithm.getChange(expected));

        assertTrue(expected.compareTo(changeValue) == 0);
    }

    @Test
    public void testGetChangeSingleCoin() {
        BigDecimal expected = BigDecimal.valueOf(1);
        BigDecimal changeValue = CoinDenomination.ValueCounter.count(algorithm.getChange(expected));

        assertTrue(expected.compareTo(changeValue) == 0);
    }
}
