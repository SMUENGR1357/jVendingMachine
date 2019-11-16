package VMDriver;

import Networking.GoogleSheetsDB;
import Structs.Item;
import Structs.User;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class VendingMachineDB {

    private ConcurrentHashMap<Integer, Item> items;
    private ConcurrentHashMap<String, Integer> teamCredit;
    private ConcurrentHashMap<Long, User> users;

    private boolean dbUpdatedItems;
    private boolean dbUpdatedCredits;

    private Thread syncThread;

    public VendingMachineDB() {
        items = new ConcurrentHashMap<>();
        teamCredit = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();

        try {
            GoogleSheetsDB.init();
            GoogleSheetsDB.loadUsers(users);
            GoogleSheetsDB.loadItems(items);
            GoogleSheetsDB.loadCredits(teamCredit);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Because data loading failed, the vending machine will exit.");
            System.exit(1);
        }

        syncThread = new SyncThread();
        syncThread.start();

    }

    public void pullDatabaseUpdates() throws Exception{
        items.clear();
        teamCredit.clear();
        users.clear();
        GoogleSheetsDB.loadItems(items);
        GoogleSheetsDB.loadCredits(teamCredit);
        GoogleSheetsDB.loadUsers(users);
    }

    public void pushDatabaseUpdates() throws Exception{
        GoogleSheetsDB.writeItems(items);
        GoogleSheetsDB.writeCredits(teamCredit);
        GoogleSheetsDB.writeUsers(users);
    }

    public int getCredit(User user) {
        if (user.admin)
            return Integer.MAX_VALUE;
        return teamCredit.get(user.team);
    }

    public void subtractFunds(User user, int cost) {
        if (!user.admin) {
            teamCredit.put(user.team, teamCredit.get(user.team) - cost);
            dbUpdatedCredits = true;
        }
    }

    public int checkQuantity(int slot) {
        return items.get(slot).quantity;
    }

    public boolean itemStocked(int slot) {
        return checkQuantity(slot) > 0;
    }

    public void decrementStock(int slot) {
        items.get(slot).quantity--;
        dbUpdatedItems = true;
    }

    public User getUser(long ID) {
        return users.get(ID);
    }

    public Item getItem(int ID) {
        return items.get(ID);
    }

    private class SyncThread extends Thread {
        private final int WAIT_TIME = 5 * 60 * 1000; //Check if a write to file is necessary every 5 minutes

        public void run() {
            while (true) {
                System.out.println("Updating files...");
                if (dbUpdatedItems) {
                    GoogleSheetsDB.writeItems(items);
//                    System.out.println("\tUpdating Items csv");
//                    File f = new File(itemsCSV);
//                    f.delete();
//                    try {
//                        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
//                        writer.write("ItemName,Cost,Location,Stock\n"); //Write header
//                        for (Map.Entry<Integer, Item> entry : items.entrySet()) {
//                            writer.write(entry.getValue().name + "," +
//                                    entry.getValue().cost + "," +
//                                    entry.getValue().slot + "," +
//                                    entry.getValue().quantity + "\n");
//                        }
//                        writer.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    dbUpdatedItems = false;
                }
                if (dbUpdatedCredits) {
                    GoogleSheetsDB.writeCredits(teamCredit);
//                    File f = new File(creditsCSV);
//                    System.out.println("\tUpdating credits CSV");
//                    f.delete();
//                    try {
//                        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
//                        writer.write("Team,Credit\n"); //Write header
//                        for (Map.Entry<String, Integer> entry : teamCredit.entrySet()) {
//                            writer.write(entry.getKey() + "," + entry.getValue() + "\n");
//                        }
//                        writer.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    dbUpdatedCredits = false;
                }
                System.out.println("Done updating files.");
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
