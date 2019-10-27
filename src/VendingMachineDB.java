import java.io.*;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class VendingMachineDB {

    private ConcurrentHashMap<Integer, Item> itemStock;
    private ConcurrentHashMap<String, Integer> teamCredit;
    private ConcurrentHashMap<Long, User> users;

    private boolean dbUpdatedItems;
    private boolean dbUpdatedCredits;

    private final String itemsCSV;
    private final String usersCSV;
    private final String creditsCSV;
    private Thread syncThread;

    public VendingMachineDB(String itemCSV, String userCSV, String creditCSV) {
        itemsCSV = itemCSV;
        usersCSV = userCSV;
        creditsCSV = creditCSV;
        itemStock = new ConcurrentHashMap<>();
        teamCredit = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
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
            reader.close();
            reader = new BufferedReader(new FileReader(new File(creditsCSV)));
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                StringTokenizer sT = new StringTokenizer(line, ",\n");
                teamCredit.put(sT.nextToken(), Integer.parseInt(sT.nextToken()));
            }
            reader.close();
            reader = new BufferedReader(new FileReader(new File(usersCSV)));
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                StringTokenizer sT = new StringTokenizer(line, ",\n");
                Long id = Long.parseLong(sT.nextToken());
                String name = sT.nextToken();
                boolean admin = sT.nextToken().equals("1") ? true : false;
                String team = sT.nextToken();
                users.put(id, new User(name, team, id, admin));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        syncThread = new SyncThread();
        syncThread.start();
    }

    public int getCredit(User user) {
        if (user.admin)
            return Integer.MAX_VALUE;
        return teamCredit.get(user.team);
    }

    public void subtractFunds(User user, int cost) {
        if (!user.admin)
            teamCredit.put(user.team, teamCredit.get(user.team) - cost);
    }

    public int checkQuantity(int slot) {
        return itemStock.get(slot).quantity;
    }

    public boolean itemStocked(int slot) {
        return checkQuantity(slot) > 0;
    }

    public void decrementStock(int slot) {
        itemStock.get(slot).quantity--;
        dbUpdatedItems = true;
    }


    private class SyncThread extends Thread {
        private final int WAIT_TIME = 10 * 60 * 1000; //Check if a write to file is necessary every 10 minutes

        public void run() {
            while (true) {
                if (dbUpdatedItems) {
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
                    dbUpdatedItems = false;
                }
                if (dbUpdatedCredits) {
                    File f = new File(creditsCSV);
                    f.delete();
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                        writer.write("Team,Credit\n"); //Write header
                        for (Map.Entry<String, Integer> entry : teamCredit.entrySet()) {
                            writer.write(entry.getKey() + "," + entry.getValue() + "\n");
                        }
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dbUpdatedCredits = false;
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
