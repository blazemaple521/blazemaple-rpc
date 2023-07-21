package com.blazemaple.exceptions;

/**
 * @description 注册中心异常
 *
 * @author BlazeMaple
 * @date 2023/7/21 2:44
 */
public class DiscoveryException extends RuntimeException{
    
    public DiscoveryException() {
    }
    
    public DiscoveryException(String message) {
        super(message);
    }
    
    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}