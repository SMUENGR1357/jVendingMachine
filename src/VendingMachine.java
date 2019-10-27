import com.fazecast.jSerialComm.SerialPort;

public class VendingMachine {

    private VendingMachineDB database;
    private SerialPort serialPort;
    public static final String COMPORT = "COM1";

    public VendingMachine() {
        setupComms();
        setupDatabase();
    }

    private void setupComms() {
        serialPort = SerialPort.getCommPort(COMPORT);
        serialPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        serialPort.openPort();
        try {
            System.out.println("Sleeping to wait for Arduino to reset...");
            Thread.sleep(20000);
            System.out.println("Sleeping complete!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupDatabase() {
        database = new VendingMachineDB("VM_Items.csv", "VM_Users.csv", "VM_Credits.csv");
    }


    public boolean vendItem(int itemID, long userID) {
        Item toVend = database.getItem(itemID);
        User vendTo = database.getUser(userID);
        if (!database.itemStocked(toVend.slot)) {
            System.out.println("No stock was found for item " + toVend.name);
            return false;
        }
        if(database.getCredit(vendTo) < toVend.cost){
            System.out.println("Not enough $$ available for user " + vendTo.name);
        }

        String bufferedID = "" + toVend.slot;
        if (bufferedID.length() < 2)
            bufferedID = "0" + bufferedID;
        String toWrite = "*" + bufferedID.charAt(0) + "*" + bufferedID.charAt(1) + "*";
        System.out.println("Vending item " + toVend.name);
        serialPort.writeBytes(toWrite.getBytes(), toWrite.length());//vend item
        database.decrementStock(toVend.slot);
        database.subtractFunds(vendTo, toVend.cost);
        return true;
    }
}