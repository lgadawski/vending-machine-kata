package tdd.vendingMachine.facade.user;

import tdd.vendingMachine.CoinDenomination;
import tdd.vendingMachine.VendingMachine;
import tdd.vendingMachine.products.Product;

import java.util.List;
import java.util.Map;

/**
 * Implementation of user vending machine facade.
 *
 * @author Łukasz Gadawski
 */
public class UserVendingMachineFacadeImpl implements UserVendingMachineFacade {

    private final VendingMachine vendingMachine;

    public UserVendingMachineFacadeImpl(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public Product selectShelveNumber(int selectedShelveNumber) {
        return vendingMachine.selectShelveNumber(selectedShelveNumber);
    }

    @Override
    public String getDisplayMessage() {
        return vendingMachine.getDisplayMessage();
    }

    @Override
    public int getSelectedShelveNumber() {
        return vendingMachine.getSelectedShelveNumber();
    }

    @Override
    public void insertCoin(CoinDenomination cd) {
        vendingMachine.insertCoin(cd);
    }

    @Override
    public void cancel() {
        vendingMachine.cancel();
    }

    @Override
    public Map<Integer, List<Product>> getShelves() {
        return vendingMachine.shelves();
    }

    @Override
    public Product getReturnedProduct() {
        return vendingMachine.getReturnedProduct();
    }

    @Override
    public Map<CoinDenomination, Integer> getReturnedChange() {
        return vendingMachine.getReturnedChange();
    }
}
