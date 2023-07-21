package com.blazemaple;

import com.blazemaple.api.service.HelloBRPC;
import com.blazemaple.discovery.RegistryConfig;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/20 2:16
 */
public class ConsumerApplication {

    public static void main(String[] args) {
        ReferenceConfig<HelloBRPC> reference=new ReferenceConfig<>();
        reference.setInterface(HelloBRPC.class);

        BrpcBootstrap.getInstance()
            .application("brpc-consumer")
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .reference(reference);

        HelloBRPC helloBRPC = reference.get();
        helloBRPC.sayHello("blazemaple");
    }
}
