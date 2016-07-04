package tdd.vendingMachine.display;

/**
 * Holds possible vending machine display messages.
 *
 * @author Łukasz Gadawski
 */
// TODO put messages into properties bundles
public interface DisplayMessages {

    String HELLO_MESSAGE = "Hello customer, please select shelve number with concrete product.";

    String NO_PRODUCTS_ON_SHELVE = "There is no products on selected shelve!";

    String SELECTED_SHELVE_NO_OUT_OF_POSSIBLE_SHELVE_NUMBERS = "Selected shelve number out of shelve number ranges!";

    String MAX_MACHINE_COIN_CAPACITY_REACHED =
        "Inserted coins reach maximum capacity for vending machine. Transaction will be canceled.";

    String NO_COINS_TO_RETURN = "There is not enough coins in machine to return change.";
}
