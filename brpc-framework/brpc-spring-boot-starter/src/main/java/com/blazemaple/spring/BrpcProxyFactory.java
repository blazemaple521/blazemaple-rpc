package com.blazemaple.spring;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.BrpcConfig;
import com.blazemaple.ReferenceConfig;
import com.blazemaple.discovery.RegistryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/8 13:37
 */
@Component
public class BrpcProxyFactory {

    private  BrpcConfig brpcConfig;

    public BrpcProxyFactory(BrpcConfig brpcConfig) {
        this.brpcConfig = brpcConfig;
    }

    private static Map<Class<?>,Object> proxyCache = new ConcurrentHashMap<>(32);

    public  <T> T getProxy(Class<T> clazz){
        Object bean = proxyCache.get(clazz);
        if (bean != null){
            return (T) bean;
        }

        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(clazz);

        BrpcBootstrap.getInstance()
            .application(brpcConfig.getApplicationName())
            .group(brpcConfig.getGroup())
            .registry(new RegistryConfig(brpcConfig.getRegistryUrl()))
            .serializer(brpcConfig.getSerializeType())
            .compressor(brpcConfig.getCompressType())
            .reference(referenceConfig);

        T t = referenceConfig.get();
        proxyCache.put(clazz,t);
        return t;
    }

}
