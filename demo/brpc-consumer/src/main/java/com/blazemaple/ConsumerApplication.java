package com.blazemaple;

import com.blazemaple.api.service.HelloBRPC;
import com.blazemaple.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 2:16
 */
@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {
        ReferenceConfig<HelloBRPC> reference=new ReferenceConfig<>();
        reference.setInterface(HelloBRPC.class);

        BrpcBootstrap.getInstance()
            .application("brpc-consumer")
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .serializer("jdk")
            .compressor("gzip")
            .group("test")
            .reference(reference);

        HelloBRPC helloBRPC = reference.get();
        while (true){
            try {
                Thread.sleep(10000);
                System.out.println("=======================================================================================");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i=0;i<15;i++){
                String blazemaple = helloBRPC.sayHello("blazemaple");
                log.info("sayHello result:{}",blazemaple);
            }
        }
    }
}
