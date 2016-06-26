package tdd.vendingMachine.display;

/**
 * Represents vending machine display.
 *
 * @author Łukasz Gadawski
 */
public class Display {

    private String currentMessage;

    private Display() {}

    public Display(String helloMessage) {
        this.currentMessage = helloMessage;
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }
}
