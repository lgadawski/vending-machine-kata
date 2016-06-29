package tdd.vendingMachine.products;

import java.math.BigDecimal;

/**
 * @author Łukasz Gadawski
 */
public abstract class Product {

    private final BigDecimal price;

    public Product(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
