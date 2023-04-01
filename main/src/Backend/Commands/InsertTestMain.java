package Backend.Commands;

public class InsertTestMain {
    public static void main(String[] args) {
        Command insert = new Insert("INSERT INTO Table (ID, Name) VALUES (2, \"Jozsef\");");
    }
}
