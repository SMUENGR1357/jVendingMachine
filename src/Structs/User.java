package Structs;

public class User {

    public User(String name, String team, Long id, boolean admin) {
        this.admin = admin;
        this.name = name;
        this.team = team;
        this.id = id;
    }

    public String name, team;
    public Long id;
    public boolean admin;
}
