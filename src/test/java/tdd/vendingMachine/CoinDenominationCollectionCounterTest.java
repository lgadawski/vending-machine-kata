package tdd.vendingMachine;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Łukasz Gadawski
 */
public class CoinDenominationCollectionCounterTest {

    @Test
    public void testInserting() {
        Map<CoinDenomination, Integer> coins = Maps.newHashMap();

        coins.put(CoinDenomination.FIVE, 2);
        assertTrue(CoinDenomination.ValueCounter.count(coins).compareTo(BigDecimal.valueOf(10)) == 0);

        coins.put(CoinDenomination.ONE, 1);
        assertTrue(CoinDenomination.ValueCounter.count(coins).compareTo(BigDecimal.valueOf(11)) == 0);

        coins.put(CoinDenomination.ONE_FIFTH, 1);
        assertTrue(CoinDenomination.ValueCounter.count(coins).compareTo(BigDecimal.valueOf(11.2)) == 0);

        coins.put(CoinDenomination.ONE_TENTH, 2);
        assertTrue(CoinDenomination.ValueCounter.count(coins).compareTo(BigDecimal.valueOf(11.4)) == 0);

        coins.put(CoinDenomination.TWO, 3);
        assertTrue(CoinDenomination.ValueCounter.count(coins).compareTo(BigDecimal.valueOf(17.4)) == 0);

        coins.put(CoinDenomination.HALF, 1);
        assertTrue(CoinDenomination.ValueCounter.count(coins).compareTo(BigDecimal.valueOf(17.9)) == 0);
    }
}
