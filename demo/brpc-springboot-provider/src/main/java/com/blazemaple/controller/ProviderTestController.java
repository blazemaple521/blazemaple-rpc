package com.blazemaple.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/7 21:33
 */
@RestController
public class ProviderTestController {
    @GetMapping("/test")
    public String Hello(){
        return "hello provider!";
    }
}
