package com.blazemaple.impl;

import com.blazemaple.annotation.BrpcService;
import com.blazemaple.api.service.HelloBRPC2;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/19 16:14
 */
@BrpcService
public class HelloBRPCImpl2 implements HelloBRPC2 {
    @Override
    public String sayHello(String msg) {
        return "Hello, consumer2: " + msg + "!";
    }
}
