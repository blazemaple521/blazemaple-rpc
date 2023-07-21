package com.blazemaple.exceptions;

/**
 * @author BlazeMaple
 * @description zookeeper异常
 * @date 2023/7/20 16:47
 */
public class ZookeeperException extends RuntimeException {

    public ZookeeperException() {
    }

    public ZookeeperException(Throwable cause) {
        super(cause);
    }
}