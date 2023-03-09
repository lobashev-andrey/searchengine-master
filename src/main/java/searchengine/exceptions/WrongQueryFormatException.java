package searchengine.exceptions;

public class WrongQueryFormatException extends Exception{
    public WrongQueryFormatException(String message) {
        super(message);
    }
}
