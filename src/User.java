public class User {

    public User(String name, String team, Long id, boolean admin) {
        this.admin = admin;
        this.name = name;
        this.team = team;
        this.id = id;
    }

    String name, team;
    Long id;
    boolean admin;
}
