package tdd.vendingMachine.products;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Łukasz Gadawski
 */
public abstract class Product implements Serializable {

    private final BigDecimal price;

    public Product(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;

        Product product = (Product) o;

        return price.equals(product.price);

    }

    @Override
    public int hashCode() {
        return price.hashCode();
    }
}
