package tdd.vendingMachine;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

/**
 * Represents coin denomination.
 *
 * @author Łukasz Gadawski
 */
public enum CoinDenomination {

    FIVE(BigDecimal.valueOf(5)),
    TWO(BigDecimal.valueOf(2)),
    ONE(BigDecimal.ONE),
    HALF(BigDecimal.valueOf(0.5)),
    ONE_FIFTH(BigDecimal.valueOf(0.2)),
    ONE_TENTH(BigDecimal.valueOf(0.1));

    private final BigDecimal value;

    CoinDenomination(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    /**
     * Descending order comparator by CoinDenomination value.
     */
    public static Comparator<CoinDenomination> valueDescendingComparator =
        (cd1, cd2) -> cd2.getValue().compareTo(cd1.getValue());

    /**
     * Counts value of coins in provided collection.
     */
    public static class ValueCounter {

        private ValueCounter() {}

        public static BigDecimal count(Map<CoinDenomination, Integer> coins) {
            Preconditions.checkNotNull(coins);

            BigDecimal result = BigDecimal.ZERO;

            for (Map.Entry<CoinDenomination, Integer> entry : ImmutableMap.copyOf(coins).entrySet()) {
                BigDecimal cdValue = entry.getKey().getValue();
                Integer cdCount = entry.getValue();
                result = result.add(cdValue.multiply(BigDecimal.valueOf(cdCount)));
            }

            return result;
        }
    }
}
