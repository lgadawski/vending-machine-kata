package tdd.vendingMachine;

import java.math.BigDecimal;

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
}
