import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
        currentTimeout = new PageTimeout();
    }

    private static String userInput = "";

    public static void keyTyped(KeyEvent e) {
        if (current == PAGE.HOME && userInput.indexOf("?+") > -1 && userInput.indexOf("?", userInput.indexOf("?+") + 1) > -1) {
            String ID = userInput.substring(userInput.indexOf("+") + 1, userInput.indexOf("=", userInput.indexOf("+") - 1));
            System.out.println("User " + ID + " swiped card.");
            if (ID.length() != 8)
                userInput = "";
            else
                swipeUser(Long.parseLong(ID));
        } else userInput += e.getCharacter();

        if (e.getCharacter().compareTo(" ") == -19)/*Numpad enter key*/ {
            switch (current) {
                case HOME: {
                    Long ID = Long.parseLong(userInput);
                    if(("" + ID).length() == 8)
                    swipeUser(Long.parseLong(userInput));
                    else userInput="";
                    break;
                }
                case SWIPED: {
                    purchaseItem(Integer.parseInt(userInput));
                    break;
                }
            }
            userInput = "";
        }
    }

    private static long currentUser;

    private static PageTimeout currentTimeout;

    public static void swipeUser(long userID) {
        System.out.println("USER ID: " + userID);
        currentUser = userID;
        vendGUI.loadPage(PAGE.SWIPED.ordinal());
        currentTimeout.finishTimeout = true;
        currentTimeout.start();
    }

    private static class PageTimeout extends Thread {
        final static long TIMEOUT_TIME = 25000; //Timeout back to main in 25 seconds
        boolean finishTimeout = true;

        public void run() {
            try {
                Thread.sleep(TIMEOUT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (finishTimeout) {
                vendGUI.loadPage(PAGE.HOME.ordinal());
                currentUser = 0;
            }
        }
    }

    public static void purchaseItem(int itemID) {
        currentTimeout.stop();
        currentTimeout.finishTimeout = true;
        currentTimeout.start();
        vendingMachine.vendItem(itemID, currentUser);
        vendGUI.loadPage(PAGE.HOME.ordinal());
        currentUser = 0;
        currentTimeout.stop();
    }

}
