package VMDriver;

import Structs.Item;
import Structs.User;
import com.fazecast.jSerialComm.SerialPort;

public class VendingMachine {

    private VendingMachineDB database;
    private SerialPort serialPort;
    public static final String COMPORT = "/dev/ttyUSB0";

    public VendingMachine() {
        setupDatabase();
        setupComms();
    }

    private void setupComms() {
        // Configure and open serial port for communicating with the Arduino which controls vending spindles
        serialPort = SerialPort.getCommPort(COMPORT);
        serialPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        serialPort.openPort();

        try {
            // Arduino devices reset after you connect to them. Code sleeps here to give the device time to do that
            System.out.println("Sleeping to wait for Arduino to reset...");
            Thread.sleep(20000);
            System.out.println("Sleeping complete!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupDatabase() {
        database = new VendingMachineDB();
    }


    public boolean vendItem(int itemID, long userID) {
        Item toVend = getItem(itemID);
        User vendTo = getUser(userID);
        if (!database.itemStocked(toVend.slot)) {
            System.out.println("No stock was found for item " + toVend.name);
            return false;
        }
        if (database.getCredit(vendTo) < toVend.cost) {
            System.out.println("Not enough $$ available for user " + vendTo.name);
        }
        String bufferedID = "" + toVend.slot;
        if (bufferedID.length() < 2)
            bufferedID = "0" + bufferedID;
        // Format of output to Arduino is *#*#, where ## is the two-digit representation of the item ID to vend
        String toWrite = "*" + bufferedID.charAt(0) + "*" + bufferedID.charAt(1) + "*";
        System.out.println("Vending item " + toVend.name);

        serialPort.writeBytes(toWrite.getBytes(), toWrite.length());// vend item
        database.decrementStock(toVend.slot);
        database.subtractFunds(vendTo, toVend.cost);
        return true;
    }

    public User getUser(long id) {
        return database.getUser(id);
    }

    public int getCredit(long id) {
        return database.getCredit(database.getUser(id));
    }

    public Item getItem(int itemID){
        return database.getItem(itemID);
    }
}
