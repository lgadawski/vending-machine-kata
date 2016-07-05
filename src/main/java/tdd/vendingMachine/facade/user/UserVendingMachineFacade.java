package tdd.vendingMachine.facade.user;

import tdd.vendingMachine.CoinDenomination;
import tdd.vendingMachine.products.Product;

import java.util.List;
import java.util.Map;

/**
 * User vending machine facade.
 *
 * @author Łukasz Gadawski
 */
public interface UserVendingMachineFacade {

    /**
     * Selects shelve number in vending machine. If selected shelve number is out of range or if
     * there is not any product on selected shelve appropriate message is displayed
     * and selected shelve number is set to default value -1. Otherwise display shelve number is saved
     * and display shows product price.
     *
     * @return product from selected shelve
     */
    Product selectShelveNumber(int selectedShelveNumber);

    /**
     * Inserting coin opening transaction. If before inserting coin there wasn't selected any shelve coin will
     * be put into returnChange collection.
     *
     * @param cd inserted coin denomination
     */
    void insertCoin(CoinDenomination cd);

    /**
     * Canceling current transaction if there is any open. If so it cancel transaction, removes inserted coins
     * from vending machine and return map that represents inserted coins. Canceling transaction is possible
     * if there was selected shelve to get product from.
     */
    void cancel();

    String getDisplayMessage();

    int getSelectedShelveNumber();

    /**
     * Readable collection copy of vending machine shelves.
     */
    Map<Integer, List<Product>> getShelves();

    Product getReturnedProduct();

    Map<CoinDenomination, Integer> getReturnedChange();
}
