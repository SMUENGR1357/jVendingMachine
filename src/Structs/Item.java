package Structs;

public class Item {
    public String name;
    public int slot;
    public int quantity;
    public int cost;

    public Item(String name, int cost, int slot, int quantity) {
        this.name = name;
        this.cost = cost;
        this.slot = slot;
        this.quantity = quantity;
    }
}
