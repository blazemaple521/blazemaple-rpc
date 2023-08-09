package com.blazemaple;

import com.blazemaple.discovery.RegistryConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/7 21:50
 */
@Component
@Slf4j
public class BrpcStarter implements CommandLineRunner {

    @Resource
    private BrpcClient brpcClient;

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("BrpcStarter is running...");
        brpcClient.start();
    }
}
