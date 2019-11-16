package VMDriver;

import Structs.User;
import UserInterface.VendingMachineGUI;
import javafx.scene.input.KeyEvent;

public class MainDriver {

    public static VendingMachine vendingMachine;
    public static VendingMachineGUI vendGUI;

    enum PAGE {
        LOADING,
        HOME,
        SWIPED,
        ADMIN,
        CONFIRM
    }

    public static PAGE current;

    public static void main(String[] args) {
        vendGUI = new VendingMachineGUI();
        current = PAGE.LOADING;
        vendGUI.addPage(PAGE.LOADING.ordinal(), "pages/loadingPage.html");
        vendGUI.addPage(PAGE.HOME.ordinal(), "pages/homePage.html");
        vendGUI.addPage(PAGE.SWIPED.ordinal(), "pages/swipedPage.html");
        vendGUI.addPage(PAGE.CONFIRM.ordinal(), "pages/confirmPurchasePage.html");
        vendGUI.addPage(PAGE.ADMIN.ordinal(), "pages/adminPage.html");
        vendGUI.loadPage(PAGE.LOADING.ordinal());
        vendingMachine = new VendingMachine();
        vendGUI.loadPage(PAGE.HOME.ordinal());
        current = PAGE.HOME;
        currentTimeout = new PageTimeout();
        currentTimeout.start();
    }

    private static String userInput = "";

    public static void keyTyped(KeyEvent e) {
        System.out.println(e.getCharacter());
        if (e.getCharacter().equals(".")) {
            userInput = userInput.substring(0, Math.max(0, userInput.length() - 1));
        } else
            userInput += e.getCharacter();

        switch (current) {
            case SWIPED: {
                if (e.getCharacter().equals("-")) {
                    userInput = "";
                    currentItem = 0;
                    currentUser = null;
                    current = PAGE.LOADING;
                    vendGUI.loadPage(PAGE.HOME.ordinal());
                    current = PAGE.HOME;
                }
                break;
            }

            case LOADING: {
                return;
            }

            case HOME: {
                if (e.getCharacter().compareTo(" ") == -19 && userInput.indexOf("=084") > -1
                        && userInput.indexOf("?+", userInput.indexOf("=084")) > -1) {
                    try {
                        String ID = userInput.substring(userInput.indexOf("=084") + 4, userInput.indexOf("?+", userInput.indexOf("=084")));
                        System.out.println("Structs.User " + ID + " swiped card.");
                        userInput = "";
                        if (ID.length() == 8) {
                            swipeUser(Long.parseLong(ID));
                        }
                    } catch (Exception ex) {
                        System.out.println("An exception occurred on swipeUser.");
                    }
                }
                break;
            }

            default: {
            }
        }

        if (e.getCharacter().compareTo(" ") == -19)/*Numpad enter key*/ {
            switch (current) {
                case HOME: {
                    Long ID = 0L;
                    try {
                        ID = Long.parseLong(userInput.trim());
                    } catch (Exception exception) {
                        System.out.println("\tCould not parse ID " + userInput);
                    }
                    if (("" + ID).length() == 8) {
                        System.out.println("User " + ID + " entered ID number.");
                        userInput = "";
                        swipeUser(ID);
                    }
                    break;
                }
                case SWIPED: {
                    if (userInput.trim().length() > 0)
                        purchaseItem(Integer.parseInt(userInput.trim()));
                    break;
                }
                case CONFIRM: {
                    vendItem(currentItem);
                }
                case ADMIN: {
                    if (userInput.trim().length() > 0) {
                        int option = Integer.parseInt(userInput.trim());
                        System.out.println("Admin option " + option);
                        switch (option) {
                            case 1:
                                swipeUser(currentUser.id, false);
                                break;
                            case 2:
                                System.out.println("Pulling updates...");
                                current = PAGE.LOADING;
                                vendGUI.loadPage(PAGE.LOADING.ordinal());
                                vendingMachine.pullUpdatesDB();
                                changePage(PAGE.HOME);
                                break;
                            case 3:
                                System.out.println("Pushing updates...");
                                current = PAGE.LOADING;
                                vendGUI.loadPage(PAGE.LOADING.ordinal());
                                vendingMachine.pushUpdatesDB();
                                changePage(PAGE.HOME);
                                break;
                            default:
                                return;
                        }
                    }
                }
            }
            userInput = "";
        } else {
            if (current == PAGE.CONFIRM) {
                changePage(PAGE.HOME);
            }
        }
    }

    private static User currentUser;
    private static int currentItem;

    private static PageTimeout currentTimeout;

    public static void swipeUser(long userID, boolean adminEnabled) {
        System.out.println("USER ID: " + userID);
        current = PAGE.LOADING;
        if (!adminEnabled) {
            System.out.println("Entering student menu.");
            vendGUI.modifyPageHTML(PAGE.SWIPED.ordinal(), "userName", currentUser.name);
            vendGUI.modifyPageHTML(PAGE.SWIPED.ordinal(), "userBalance", "" + vendingMachine.getCredit(currentUser.id));
            vendGUI.loadPage(PAGE.SWIPED.ordinal());
            current = PAGE.SWIPED;
        } else {
            System.out.println("Entering admin menu.");
            current = PAGE.LOADING;
            vendGUI.loadPage(PAGE.ADMIN.ordinal());
            current = PAGE.ADMIN;
        }
        userInput = "";
        currentTimeout.reset();

    }

    public static void swipeUser(long userID) {
        currentUser = vendingMachine.getUser(userID);
        swipeUser(userID, currentUser.admin);
    }

    private static class PageTimeout extends Thread {
        final static long TIMEOUT_TIME = 25000; //Timeout back to main in 25 seconds
        static long currentTimeout;

        public void run() {
            while (true) {
                currentTimeout = TIMEOUT_TIME;
                try {
                    while ((currentTimeout -= 1500) > 0)
                        Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                current = PAGE.LOADING;
                changePage(PAGE.HOME);
            }
        }

        public void reset() {
            currentTimeout = TIMEOUT_TIME;
        }
    }

    public static void purchaseItem(int itemID) {
        System.out.println("Confirming purchase of item " + itemID);
        currentItem = itemID;
        currentTimeout.reset();
        current = PAGE.LOADING;
        vendGUI.modifyPageHTML(PAGE.CONFIRM.ordinal(), "itemName", vendingMachine.getItem(itemID).name);
        vendGUI.modifyPageHTML(PAGE.CONFIRM.ordinal(), "itemCost", "" + vendingMachine.getItem(itemID).cost);
        vendGUI.loadPage(PAGE.CONFIRM.ordinal());
        current = PAGE.CONFIRM;

    }

    public static void vendItem(int itemID) {
        System.out.println("Vending item " + itemID);
        vendingMachine.vendItem(itemID, currentUser.id);
        changePage(PAGE.HOME);
    }

    public static void changePage(PAGE changeTo) {
        userInput = "";
        currentUser = null;
        currentItem = 0;
        current = PAGE.LOADING;
        vendGUI.loadPage(changeTo.ordinal());
        current = changeTo;
    }

}
