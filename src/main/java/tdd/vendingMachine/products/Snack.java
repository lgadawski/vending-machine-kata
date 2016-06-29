package tdd.vendingMachine.products;

import java.math.BigDecimal;

/**
 * @author Łukasz Gadawski
 */
public class Snack extends Product {

    private final float weight;

    public Snack(BigDecimal price, float weight) {
        super(price);
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }
}
