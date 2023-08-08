package com.blazemaple.impl;

import com.blazemaple.annotation.BrpcService;
import com.blazemaple.api.service.BrpcTestService;


/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/7 21:28
 */
@BrpcService(group = "test")
public class BrpcTestServiceImpl implements BrpcTestService {

    @Override
    public String HelloBrpc(String name) {
        return "Hello " + name;
    }
}
