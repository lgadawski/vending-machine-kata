package tdd.vendingMachine.display;

/**
 * Represents vending machine display.
 *
 * @author Łukasz Gadawski
 */
public class Display {

    private String message;

    public Display() {
        // empty
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
