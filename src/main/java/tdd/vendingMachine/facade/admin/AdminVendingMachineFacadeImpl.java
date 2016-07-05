package tdd.vendingMachine.facade.admin;

import tdd.vendingMachine.VendingMachine;
import tdd.vendingMachine.products.Product;

import java.util.Map;

/**
 * Implementation of administration interface for vending machine.
 *
 * @author Łukasz Gadawski.
 */
public class AdminVendingMachineFacadeImpl implements AdminVendingMachineFacade {

    private final VendingMachine vendingMachine;

    public AdminVendingMachineFacadeImpl(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void feedWithCoinEachType(int coinNumber) {
        vendingMachine.feedWithCoinsEachType(coinNumber);
    }

    @Override
    public void feedWithProducts(Map<Integer, Product> products) {
        vendingMachine.feedWithProducts(products);
    }
}
