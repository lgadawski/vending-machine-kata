package tdd.vendingMachine.changeAlgorithm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import tdd.vendingMachine.CoinDenomination;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Algorithm for counting change for given value.
 *
 * At creation algorithm instance provided coins are put into map sorted by coin denomination value in descending order.
 *
 * @author Łukasz Gadawski
 */
public class CoinReturningAlgorithm {

    private final Map<CoinDenomination, Integer> availableCoins;

    public CoinReturningAlgorithm(final Map<CoinDenomination, Integer> availableCoins) {
        Preconditions.checkNotNull(availableCoins);

        this.availableCoins = Maps.newTreeMap(CoinDenomination.valueDescendingComparator);
        this.availableCoins.putAll(availableCoins);
    }

    /**
     * For given overflow amount gives change base on provided map of coins.
     *
     * @param overFlow - amount to return in coins
     * @return - map of coins needed to return change based on provided initially coins
     * @throws NotEnoughCoinsToReturnException - when there is not enough coins to return properly change
     */
    public Map<CoinDenomination, Integer> getChange(final BigDecimal overFlow) throws NotEnoughCoinsToReturnException {
        Preconditions.checkNotNull(overFlow);

        int zeroToOverflow = BigDecimal.ZERO.compareTo(overFlow);
        Preconditions.checkArgument(zeroToOverflow <= 0);

        if (zeroToOverflow == 0) {
            return Collections.emptyMap();
        }
        if (availableCoins.isEmpty()) {
            throw new NotEnoughCoinsToReturnException();
        }

        BigDecimal leftToReturn = overFlow;
        Map<CoinDenomination, Integer> result = Maps.newHashMap();
        for (Iterator<Map.Entry<CoinDenomination, Integer>> it = availableCoins.entrySet().iterator();
             it.hasNext() && !(BigDecimal.ZERO.compareTo(leftToReturn) == 0); ) {

            Map.Entry<CoinDenomination, Integer> entry = it.next();
            BigDecimal entryCoinDenominationValue = entry.getKey().getValue();
            while (entry.getValue() > 0) {
                if (entryCoinDenominationValue.compareTo(leftToReturn) <= 0) {
                    Integer resultCoinNumber = result.get(entry.getKey());
                    if (resultCoinNumber == null) {
                        result.put(entry.getKey(), 1);
                    } else {
                        result.replace(entry.getKey(), resultCoinNumber+1);
                    }
                    leftToReturn = leftToReturn.subtract(entryCoinDenominationValue);

                    // if last coin
                    if (entry.getValue() == 1) {
                        it.remove();
                        break;
                    } else {
                        availableCoins.replace(entry.getKey(), entry.getValue() - 1);
                    }
                } else {
                    // no sense to iterate over this denomination
                    break;
                }
            }
        }

        if (BigDecimal.ZERO.compareTo(leftToReturn) == 0) {
            return result;
        }

        throw new NotEnoughCoinsToReturnException();
    }
}
