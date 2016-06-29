package tdd.vendingMachine.products.liquid;

import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;

/**
 * @author Łukasz Gadawski
 */
public class Liquid extends Product {

    private final LiquidType type;

    private final double capacity;

    public Liquid(LiquidType type, BigDecimal price, double capacity) {
        super(price);
        this.type = type;
        this.capacity = capacity;
    }

    public double getCapacity() {
        return capacity;
    }

    public LiquidType getType() {
        return type;
    }
}
