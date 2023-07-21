package com.blazemaple.exceptions;

/**
 * @description 网络异常
 *
 * @author BlazeMaple
 * @date 2023/7/21 1:17
 */
public class NetworkException extends RuntimeException{
    
    public NetworkException() {
    }
    
    public NetworkException(String message) {
        super(message);
    }
    
    public NetworkException(Throwable cause) {
        super(cause);
    }
}