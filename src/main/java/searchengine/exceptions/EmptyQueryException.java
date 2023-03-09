package searchengine.exceptions;

public class EmptyQueryException extends Exception{
    public EmptyQueryException(String message){
        super(message);
    }
}
