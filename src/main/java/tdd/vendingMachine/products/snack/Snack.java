package tdd.vendingMachine.products.snack;

import tdd.vendingMachine.products.Product;

import java.math.BigDecimal;

/**
 * @author Łukasz Gadawski
 */
public class Snack extends Product {

    private final double weight;
    private final SnackType snackType;

    private Snack(Builder builder) {
        super(builder.price);
        this.weight = builder.weight;
        this.snackType = builder.type;
    }

    public double getWeight() {
        return weight;
    }

    public SnackType getSnackType() {
        return snackType;
    }

    public static class Builder {

        private SnackType type;
        private BigDecimal price;
        private double weight;

        public Builder type(SnackType type) {
            this.type = type;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder weight(double weight) {
            this.weight = weight;
            return this;
        }

        public Product build() {
            return new Snack(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Snack)) return false;
        if (!super.equals(o)) return false;

        Snack snack = (Snack) o;

        if (Double.compare(snack.weight, weight) != 0) return false;
        return snackType == snack.snackType;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + snackType.hashCode();
        return result;
    }
}
