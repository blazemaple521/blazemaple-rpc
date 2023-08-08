package com.blazemaple.exceptions;

/**
 * @description
 *
 * @author BlazeMaple
 * @date 2023/8/6 22:42
 */

public class ResponseException extends RuntimeException {
    
    private byte code;
    private String msg;
    
    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}