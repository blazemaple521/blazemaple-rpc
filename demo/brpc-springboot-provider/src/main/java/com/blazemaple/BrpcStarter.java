package com.blazemaple;

import com.blazemaple.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("BrpcStarter is running...");
        BrpcBootstrap.getInstance()
            .application("brpc-springboot-provider")
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .serializer("jdk")
            .compressor("gzip")
            .scan("com.blazemaple.impl")
            .start();
    }
}
