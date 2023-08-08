package com.blazemaple.controller;

import com.blazemaple.annotation.BrpcServiceReference;
import com.blazemaple.api.service.BrpcTestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/7 21:33
 */
@RestController
public class ConsumerTestController {

    @BrpcServiceReference
    private BrpcTestService brpcTestService;

    @GetMapping("/test")
    public String test(){
        return "hello consumer!";
    }


    @GetMapping("/hello")
    public String hello(){
        return brpcTestService.HelloBrpc("sqp");
    }
}
