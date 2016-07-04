package tdd.vendingMachine.products.liquid;

import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;

/**
 * @author Łukasz Gadawski
 */
public class Liquid extends Product {

    private final LiquidType type;
    private final double capacity;

    private Liquid(Builder builder) {
        super(builder.price);
        this.type = builder.type;
        this.capacity = builder.capacity;
    }

    public double getCapacity() {
        return capacity;
    }

    public LiquidType getType() {
        return type;
    }

    public static class Builder {

        private LiquidType type;
        private BigDecimal price;
        private double capacity;

        public Builder type(LiquidType type) {
            this.type = type;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder capacity(double capacity) {
            this.capacity = capacity;
            return this;
        }

        public Product build() {
            return new Liquid(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Liquid)) return false;
        if (!super.equals(o)) return false;

        Liquid liquid = (Liquid) o;

        if (Double.compare(liquid.capacity, capacity) != 0) return false;
        return type == liquid.type;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + type.hashCode();
        temp = Double.doubleToLongBits(capacity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
