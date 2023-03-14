package Backend.Exceptions;

public class UnknownCommandException extends Exception {
    public UnknownCommandException() {
        super("Unknown command exception!");
    }
}
