package com.blazemaple.impl;

import com.blazemaple.annotation.BrpcService;
import com.blazemaple.api.service.HelloBRPC;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/19 16:14
 */
@BrpcService(group = "test")
public class HelloBRPCImpl implements HelloBRPC {
    @Override
    public String sayHello(String msg) {
        return "Hello, consumer: " + msg + "!";
    }
}
