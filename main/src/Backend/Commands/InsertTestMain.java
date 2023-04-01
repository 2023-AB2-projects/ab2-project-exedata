package Backend.Commands;

public class InsertTestMain {
    public static void main(String[] args) {
        Command insert = new Insert("INSERT INTO db (Name, Salary) VALUES (\"Jozsef\", 5000);");
    }
}
