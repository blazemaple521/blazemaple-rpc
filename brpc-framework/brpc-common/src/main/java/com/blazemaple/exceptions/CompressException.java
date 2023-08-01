package com.blazemaple.exceptions;

import java.io.IOException;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/30 14:16
 */
public class CompressException extends RuntimeException {

    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
