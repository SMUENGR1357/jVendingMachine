import com.fazecast.jSerialComm.SerialPort;

public class VendingMachine {

    private SerialPort serialPort;
    public static final String COMPORT = "COM1";

    public VendingMachine(){
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

    public int checkQuantity(int slot){

    }

    public boolean itemStocked(int slot){
        return checkQuantity(slot)>0;
    }

    public boolean vendItem(Item toVend){
        int useIndex = 0;
        while (useIndex < toVend.slots.length && !itemStocked(toVend.slots[useIndex]))
            useIndex++;
        if(useIndex == toVend.slots.length){
            System.out.println("No stock was found for item " + toVend.name);
            return false;
        }
        String bufferedID = "" + toVend.slots[useIndex];
        if (bufferedID.length() < 2)
            bufferedID = "0" + bufferedID;
        String toWrite = "*" + bufferedID.charAt(0) + "*" + bufferedID.charAt(1) + "*";
        serialPort.writeBytes(toWrite.getBytes(), toWrite.length());
        return true;
    }
}
