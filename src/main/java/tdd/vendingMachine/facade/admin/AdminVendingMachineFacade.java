package tdd.vendingMachine.facade.admin;

import tdd.vendingMachine.products.Product;

import java.util.Map;

/**
 * Administration interface for vending machine.
 *
 * @author Łukasz Gadawski
 */
public interface AdminVendingMachineFacade {

    /**
     * Inserts into vending machine passed number of coin each possible type.
     */
    void feedWithCoinEachType(int coinNumber);

    /**
     * Inserts into machine product map.
     * @param products - map of product to insert,
     *                 keys is shelve number,
     *                 values are product type to put on shelve.
     */
    void feedWithProducts(Map<Integer, Product> products);
}
