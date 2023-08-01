package com.blazemaple.exceptions;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/30 20:29
 */
public class LoadBalancerException extends RuntimeException {

    public LoadBalancerException(String message) {
        super(message);
    }

    public LoadBalancerException() {
    }
}
