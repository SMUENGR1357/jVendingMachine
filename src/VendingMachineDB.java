import java.io.*;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class VendingMachineDB {

    private ConcurrentHashMap<Integer, Item> itemStock;
    private ConcurrentHashMap<String, Integer> teamCredit;
    private ConcurrentHashMap<Long, User> users;

    private static class User {
        public User(String name, Long id) {
            this(name, id, false);
        }

        public User(String name, Long id, boolean admin) {
            this.admin = admin;
            this.name = name;
            this.id = id;
        }

        String name;
        Long id;
        boolean admin;
    }

    private boolean dbUpdated;

    private final String itemsCSV;
    private Thread syncThread;

    public VendingMachineDB(String itemCSV) {
        itemsCSV = itemCSV;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(itemsCSV)));
            String line = reader.readLine(); //Read header
            while ((line = reader.readLine()) != null) {
                StringTokenizer sT = new StringTokenizer(line, ",\n");
                String name = sT.nextToken();
                int cost = Integer.parseInt(sT.nextToken());
                int location = Integer.parseInt(sT.nextToken());
                int stock = Integer.parseInt(sT.nextToken());
                itemStock.put(location, new Item(name, cost, location, stock));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        syncThread = new SyncThread();
        syncThread.start();
    }

    public int checkQuantity(int slot) {
        return itemStock.get(slot).quantity;
    }

    public boolean itemStocked(int slot) {
        return checkQuantity(slot) > 0;
    }

    public void decrementStock(int slot) {
        itemStock.get(slot).quantity--;
        dbUpdated = true;
    }


    private class SyncThread extends Thread {
        private final int WAIT_TIME = 10 * 60 * 1000; //Check if a write to file is necessary every 10 minutes

        public void run() {
            while (true) {
                if (dbUpdated) {
                    File f = new File(itemsCSV);
                    f.delete();
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                        writer.write("ItemName,Cost,Location,Stock\n"); //Write header
                        for (Map.Entry<Integer, Item> entry : itemStock.entrySet()) {
                            writer.write(entry.getValue().name + "," +
                                    entry.getValue().cost + "," +
                                    entry.getValue().slot + "," +
                                    entry.getValue().quantity + "\n");
                        }
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dbUpdated = false;
                }

                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
