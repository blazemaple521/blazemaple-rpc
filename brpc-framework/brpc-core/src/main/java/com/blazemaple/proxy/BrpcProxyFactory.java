package com.blazemaple.proxy;

import com.blazemaple.BrpcBootstrap;
import com.blazemaple.ReferenceConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/8/8 13:37
 */
public class BrpcProxyFactory {

    private static Map<Class<?>,Object> proxyCache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz){
        Object bean = proxyCache.get(clazz);
        if (bean != null){
            return (T) bean;
        }

        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(clazz);

        BrpcBootstrap.getInstance().reference(referenceConfig);

        T t = referenceConfig.get();
        proxyCache.put(clazz,t);
        return t;
    }

}
