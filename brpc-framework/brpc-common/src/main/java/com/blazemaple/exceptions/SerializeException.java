package com.blazemaple.exceptions;

public class SerializeException extends RuntimeException{
    
    public SerializeException() {
    }
    
    public SerializeException(String message) {
        super(message);
    }
    
    public SerializeException(Throwable cause) {
        super(cause);
    }
}