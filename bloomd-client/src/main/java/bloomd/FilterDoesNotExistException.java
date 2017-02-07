package bloomd;

public class FilterDoesNotExistException extends RuntimeException {
    public FilterDoesNotExistException(String message) {
        super(message);
    }
}
