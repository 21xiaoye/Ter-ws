package org.ws.exceptions;

public class NotSendAbleException extends RuntimeException{
    private static final long serialVersionUID = -6468967874576651628L;

    public NotSendAbleException(String message){
        super(message);
    }
    public NotSendAbleException(Throwable throwable){
        super(throwable);
    }

    public NotSendAbleException(String message, Throwable throwable){
        super(message, throwable);
    }
}
