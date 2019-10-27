import java.awt.event.KeyEvent;

public class MainDriver {

    public static VendingMachine vendingMachine;
    public static VendingMachineGUI vendGUI;

    enum PAGE {
        HOME,
        SWIPED,
        CONFIRM
    }

    public static PAGE current;

    public static void main(String[] args) {
        //   vendingMachine = new VendingMachine();
        vendGUI = new VendingMachineGUI();
        vendGUI.addPage(PAGE.HOME.ordinal(), "pages/homePage.html");
        vendGUI.addPage(PAGE.SWIPED.ordinal(), "pages/swipedPage.html");
        vendGUI.addPage(PAGE.CONFIRM.ordinal(), "pages/confirmPurchasePage.html");
        vendGUI.loadPage(PAGE.HOME.ordinal());
        current = PAGE.HOME;
    }

    private static String userInput = "";

    public static void keyTyped(KeyEvent e) {
        if (e.getKeyChar() != '=')
            userInput += e.getKeyChar();
        else {

            switch (current) {
                case HOME: {
                    swipeUser(Long.parseLong(userInput));
                    break;
                }
                case SWIPED: {

                    break;
                }
            }


            userInput = "";
        }


    }

    public static void swipeUser(long userID) {
        vendGUI.loadPage(PAGE.SWIPED.ordinal());

    }

}
