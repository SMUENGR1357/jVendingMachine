public class Item {
    public String name;
    public int slot;
    public int quantity;
    public int cost;

    public Item(String name, int cost, int slots, int quantity) {
        this.name = name;
        this.cost = cost;
        this.slot = slots;
        this.quantity = quantity;
    }
}
