package tdd.vendingMachine;

/**
 * Vending machine configuration.
 *
 * @author Łukasz Gadawski.
 */
public class VendingMachineConfig {

    private final int maxCoinNumberOfEachTypeInVendingMachine;
    private final int maxProductsOnShelve;
    private final int numberOfShelves;

    private VendingMachineConfig(Builder builder) {
        this.maxCoinNumberOfEachTypeInVendingMachine = builder.maxCoinNumberOfEachTypeInVendingMachine;
        this.maxProductsOnShelve = builder.maxProductsOnShelve;
        this.numberOfShelves = builder.numberOfShelves;
    }

    public int getMaxCoinNumberOfEachTypeInVendingMachine() {
        return maxCoinNumberOfEachTypeInVendingMachine;
    }

    public int getMaxProductsOnShelve() {
        return maxProductsOnShelve;
    }

    public int getNumberOfShelves() {
        return numberOfShelves;
    }

    public static class Builder {

        private int maxCoinNumberOfEachTypeInVendingMachine = 0;
        private int numberOfShelves = 0;
        private int maxProductsOnShelve = 0;

        public Builder setMaxCoinNumberOfEachTypeInVendingMachine(int maxCoinNumberOfEachTypeInVendingMachine) {
            this.maxCoinNumberOfEachTypeInVendingMachine = maxCoinNumberOfEachTypeInVendingMachine;
            return this;
        }

        public Builder setNumberOfShelves(int numberOfShelves) {
            this.numberOfShelves = numberOfShelves;
            return this;
        }

        public Builder setMaxProductsOnShelve(int maxProductsOnShelve) {
            this.maxProductsOnShelve = maxProductsOnShelve;
            return this;
        }

        public VendingMachineConfig build() {
            return new VendingMachineConfig(this);
        }
    }
}
